package com.qr.app.ui;

import com.qr.app.backend.Configurer;
import com.qr.app.backend.entity.HierarchyOfBoxes;
import com.qr.app.backend.entity.db.StateDB;
import com.qr.app.backend.entity.db.Transaction;
import com.qr.app.backend.entity.forSession.LogSession;
import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.entity.forSession.temporarytable.container.ContainerBox;
import com.qr.app.backend.entity.forSession.temporarytable.container.ContainerContent;
import com.qr.app.backend.entity.order.box.Box;
import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.entity.order.container.DescriptionContainer;
import com.qr.app.backend.entity.order.container.VariantContainer;
import com.qr.app.backend.repository.HierarchyOfBoxesRepository;
import com.qr.app.backend.repository.LogSessionRepository;
import com.qr.app.backend.repository.db.StateDBRepository;
import com.qr.app.backend.repository.db.TransactionRepository;
import com.qr.app.backend.repository.order.box.BoxRepository;
import com.qr.app.backend.repository.order.container.ContainerRepository;
import com.qr.app.backend.repository.order.container.DescriptionContainerRepository;
import com.qr.app.backend.repository.sound.SoundRepository;
import com.qr.app.backend.repository.temporary.container.ContainerBoxRepository;
import com.qr.app.backend.repository.temporary.container.ContainerContentRepository;
import com.qr.app.backend.sound.Sound;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Route (value = "container")
@Push
public class ContainerView extends VerticalLayout {

    private Grid<ContainerContent> grid;
    private Label numberBoxLabel = new Label("Номер короба: ");
    private Label inBoxNeedLabel = new Label("Надо: ");
    private Label inBoxNowLabel = new Label("Собрано: ");
    private TextField numberBox = new TextField();
    private TextField inBoxNeed = new TextField();
    private TextField inBoxNow = new TextField();

    private Dialog dialog = new Dialog();
    private String bufferCode;

    private SerialPort serialPort;

    private boolean isStarted = false;
    private List<String> historyBox;
    private String macAddress;
    private boolean firtsCheck = false;

    private final ContainerRepository containerRepo;
    private final DescriptionContainerRepository descriptionContRepo;
    private final BoxRepository boxRepo;
    private final ContainerContentRepository contContentRepo;
    private final ContainerBoxRepository contBoxRepo;
    private final LogSessionRepository logSessionRepository;
    private final StateDBRepository stateDBRepository;
    private final TransactionRepository transactionRepository;
    private final SoundRepository soundRepository;
    private final HierarchyOfBoxesRepository hierarchyOfBoxesRepo;

    public ContainerView (ContainerRepository containerRepo, DescriptionContainerRepository descriptionContRepo, BoxRepository boxRepo, ContainerContentRepository contContentRepo, ContainerBoxRepository contBoxRepo, LogSessionRepository logSessionRepository, StateDBRepository stateDBRepository, TransactionRepository transactionRepository, SoundRepository soundRepository, HierarchyOfBoxesRepository hierarchyOfBoxesRepo) throws FileNotFoundException {
        this.containerRepo = containerRepo;
        this.descriptionContRepo = descriptionContRepo;
        this.boxRepo = boxRepo;
        this.contContentRepo = contContentRepo;
        this.contBoxRepo = contBoxRepo;
        this.logSessionRepository = logSessionRepository;
        this.stateDBRepository = stateDBRepository;
        this.transactionRepository = transactionRepository;
        this.soundRepository = soundRepository;
        this.hierarchyOfBoxesRepo = hierarchyOfBoxesRepo;
        addClassName("mark-view");
        setSizeFull();
        configureGrid();
        add(getToolBar(), grid);
        macAddress = Configurer.getMacAddress();
        if (macAddress.isEmpty()) {
            saveLog("", "Строка с mac адресом не найдена в файле. Убедитесь, что файл application.properties находится в каталоге с jar файлом", LvlEvent.SYSTEM_INFO, "");
        }
        bufferCode = "";
        historyBox = new ArrayList<>();
        serialPort = new SerialPort(Configurer.getDevPort());
        refresh:
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);

            serialPort.addEventListener(eventKey -> {
                    StringBuilder sb = new StringBuilder();
                    try {
                        sb.append(serialPort.readString(eventKey.getEventValue()));
                        UI ui = getUI().get();
                        ui.access(() -> {
                            String data = sb.toString();
                            if (data.length() <= 255) {
                                saveLog("", "Пришло " + data, LvlEvent.SYSTEM_INFO, macAddress);
                                int symbol = sb.indexOf("\r\n");
                                if (symbol != -1){
                                    bufferCode = bufferCode.concat(data.substring(0, symbol));
                                    analyseCode();
                                }
                                else {
                                    bufferCode += data;
                                }
                            }
                            else {
                                bufferCode = "";
                                messageToPeople("Произошла ошибка. Считайте штрихкод ещё раз, чуть медленнее.");
                                playSound("Ошибка.wav");
                            }
                        });
                    } catch (SerialPortException e) {
                        saveLog("", "Ошибка считывания значения со сканера", LvlEvent.SYSTEM_INFO, macAddress);
                        e.printStackTrace();
                    }

            }, SerialPort.MASK_RXCHAR);
        }
        catch (SerialPortException ex) {
            saveLog("", "Порт занят.", LvlEvent.CRITICAL, macAddress);
            messageToPeople("Порт занят. Проверьте, что сканер использует COM порт указанный в настройках! Выключите приложения, которые используют сканер. Перезапустите приложение");
            break refresh;
        }

    }
    // конфигурация таблицы
    public void configureGrid () {
        grid = new Grid<>(ContainerContent.class);
        grid.setSizeFull();
        grid.removeAllColumns();
        grid.addColumn(ContainerContent::getNumberVariantBox).setHeader("Номер варианта короба");
        grid.addColumn(ContainerContent::getCountNow).setHeader("Количество собрано");
        grid.addColumn(ContainerContent::getCountNeed).setHeader("Количество надо");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }
    // конфигурация текстовых полей
    public HorizontalLayout getToolBar () {

        HorizontalLayout layout = new HorizontalLayout(numberBoxLabel, numberBox, inBoxNeedLabel, inBoxNeed, inBoxNowLabel, inBoxNow);

        numberBox.setReadOnly(true);
        inBoxNeed.setReadOnly(true);
        inBoxNow.setReadOnly(true);

        return layout;

    }
    // запись событий в базу данных
    public void saveLog (String bufferCode, String descriptionEvent, LvlEvent lvlEvent, String macAddress) {
        LogSession event = new LogSession(new Date().getTime(), bufferCode, descriptionEvent, lvlEvent, macAddress);
        logSessionRepository.save(event);
    }
    // воспроизведение звука
    public void playSound(String nameSound) {
        com.qr.app.backend.entity.Sound forPlay = soundRepository.findByFilename(nameSound);

        if (!forPlay.getFilename().equals("Ок.wav")) {
            saveLog("", "Получение файла с музыкой", LvlEvent.SYSTEM_INFO, macAddress);

            File file = null;

            try {
                file = new File(new File(".").getAbsolutePath(), forPlay.getFilename());
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }

            Sound sound = null;
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    OutputStream outStream = new FileOutputStream(file);
                    outStream.write(forPlay.getSound());
                    outStream.close();
                    sound = new Sound(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    OutputStream outStream = new FileOutputStream(file);
                    outStream.write(forPlay.getSound());
                    outStream.close();
                    sound = new Sound(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (sound != null) {
                sound.play();
                //sound.join();
            }
            sound.close();
            file.delete();
        }

    }
    // вывод сообщений на экран
    public void messageToPeople (String message){

        dialog.close();

        if (!message.isEmpty()) {
            dialog = new Dialog();
            dialog.add(new Label(message));
            dialog.open();
        }

    }
    // анализ штрихкодов
    public void analyseCode() {
        if (bufferCode.length() == 18) {
            boolean lockDB = checkDBState();
            saveLog(bufferCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
            if (!lockDB) {
                if (bufferCode.charAt(0) == '2') {
                    processBarcodeContainer();
                }
                else {
                    saveLog(bufferCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
                    if (historyBox.size() == 3 ) {
                        historyBox.remove(2);
                    }
                    processBarcodeBox();
                }
            }
            else {
                okMsgDBIsLock();
            }
        }
        else if (bufferCode.length() == 20) {
            boolean lockDB = checkDBState();
            saveLog(bufferCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
            if (!lockDB) {
                if (bufferCode.charAt(2) == '2' && bufferCode.charAt(0) == '0' && bufferCode.charAt(0) == '0') {
                    processBarcodeContainer();
                }
                else {
                    saveLog(bufferCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
                    if (historyBox.size() == 3 ) {
                        historyBox.remove(2);
                    }
                    processBarcodeBox();
                }
            }
            else {
                okMsgDBIsLock();
            }
        }
        else {
            if (historyBox.size() == 3) {
                historyBox.remove(2);
            }
            errorMsgContQrDontFind();
        }
        bufferCode = "";
        lock = false;
    }
    // обработка считывания штрихкода содержимого
    public void processBarcodeBox () {
        transactionRepository.save(new Transaction(macAddress));
        saveLog(bufferCode, "Транзакция открыта", LvlEvent.CRITICAL, macAddress);
        if (isStarted) {
            if (firtsCheck) {
                saveLog(bufferCode, "Добавление короба в короб", LvlEvent.SYSTEM_INFO, macAddress);
                assemblyBox();
            }
            else {
                saveLog(bufferCode, "Добавление короба в короб. Первое сканирование", LvlEvent.SYSTEM_INFO, macAddress);
                firstScanBox();
                firtsCheck = true;
            }
        }
        else {
            errorMsgBoxAttentionBuildDontStart();
        }
        transactionRepository.delete(transactionRepository.findBySession(macAddress));
        saveLog(bufferCode, "Транзакция закрыта", LvlEvent.CRITICAL, macAddress);
    }
    // обработка первого сканирования марки пользователем
    public void firstScanBox () {
        Container cont = containerRepo.findByNumberContainer(historyBox.get(0)).orElse(new Container());
        if (cont.getNumberContainer() != null && !cont.getNumberContainer().isEmpty()) {
            String statusBox = cont.getStatus();
            if (statusBox.equals("В сборке")) {
                List<ContainerContent> contContent = contContentRepo.findByMacAddress(macAddress);
                if (contContent.size() > 0) {
                    assemblyBox();
                }
                else {
                    errorMsgContAssembledInAnotherPC(cont);
                }
            }
            else if (statusBox.equals("Собран")){
                errorMsgContAssembled();
            }
            else {
                assemblyBox();
            }
        }
        else {
            errorMsgContDontFind();
        }

    }
    // сборка короба
    public void assemblyBox() {
        int need = Integer.parseInt(inBoxNeed.getValue());
        insertBox();
        int now = Integer.parseInt(inBoxNow.getValue());
        saveLog("", "Сборка короба. Нужно собрать " + need + ". Собрано " + now, LvlEvent.SYSTEM_INFO, macAddress);
        if (need - now == 0) {
            transferData();
            clearHistory();
            messageToPeople("Сборка короба завершена!");
            playSound("Короб_собран.wav");
        }
    }
    // перенос данных из временных баз, в постоянные
    public void transferData() {
        saveLog("", "Начало переноса данных из временной базы в постоянную.", LvlEvent.SYSTEM_INFO, macAddress);
        List<ContainerBox> containerBoxes = contBoxRepo.findByMacAddress(macAddress);
        List<HierarchyOfBoxes> modifiedBoxes = new LinkedList<>();
        Container container = containerRepo.findByNumberContainer(containerBoxes.get(0).getNumberContainer()).get();
        container.setStatus("Собран");
        for (ContainerBox containerBox : containerBoxes) {
            HierarchyOfBoxes hierarchyOfBoxes = hierarchyOfBoxesRepo.findByNumberContainer(container.getNumberContainer()).orElse(new HierarchyOfBoxes());
            hierarchyOfBoxes.setDate(new Date().getTime());
            hierarchyOfBoxes.setNumberBox(containerBox.getNumberBox());
            hierarchyOfBoxes.setNumberContainer(containerBox.getNumberContainer());
            modifiedBoxes.add(hierarchyOfBoxes);
        }
        containerRepo.save(container);
        hierarchyOfBoxesRepo.saveAll(modifiedBoxes);
        saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + " завершена", LvlEvent.INFO, macAddress);
    }
    // добавление box в container
    public void insertBox () {
        // найти короб по numberbox
        // определить вариант короба
        // если в description container содержится вариант короба, то проверяю что короб не лежит в друго контейнере
        Box box = boxRepo.findByNumberBox(bufferCode).orElse(new Box());
        Container cont = containerRepo.findByNumberContainer(historyBox.get(0)).orElse(new Container());
        if (box.getStatus() != null && box.getNumberBox() != null && box.getStatus().equals("Собран") && ! box.getNumberBox().isEmpty()) {
            HierarchyOfBoxes hierarchyOfBoxes = hierarchyOfBoxesRepo.findByNumberBox(box.getNumberBox()).orElse(new HierarchyOfBoxes());
            ContainerBox contBox = contBoxRepo.findByNumberBox(box.getNumberBox()).orElse(new ContainerBox());
            if (hierarchyOfBoxes.getDate() != 0 || !contBox.getNumberBox().isEmpty()) {
                messageToPeople("Ошибка! Данный короб находится в другой упаковке!");
                saveLog(bufferCode, "Сборка упаковки " + historyBox.get(0) + ". Короб находится в другой упаковке" + hierarchyOfBoxes.getNumberContainer() + "/" + contBox.getNumberContainer(), LvlEvent.WARNING, macAddress);
                playSound("Эта_марка_уже_отгружена.wav");
            }
            else {
                ContainerContent contContent = contContentRepo.findByNumberVariantBox(box.getVariantBox().getNumberVariant()).orElse(new ContainerContent());
                if (contContent.getNumberVariantBox() != null && !contContent.getNumberVariantBox().isEmpty()) {
                    int nowCount = contContent.getCountNow();
                    int needCount = contContent.getCountNeed();
                    if (!cont.getNumberContainer().isEmpty()) {
                        if (needCount == 0) {
                            newMark(cont, bufferCode, contContent);
                        }
                        else {
                            if (needCount > nowCount) {
                                newMark(cont, bufferCode, contContent);
                            }
                            if (needCount == nowCount) {
                                messageToPeople("Ошибка! Отложите " + box.getVariantBox().getNumberVariant() + "! Сборка этих коробов закончена!");
                                saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Сборка данного вида коробов закончена!", LvlEvent.WARNING, macAddress);
                                playSound("Сборка_этого_товара_уже_закончена.wav");
                            }
                        }
                    }
                    else {
                        errorMsgContDontFind();
                    }
                }
                else {
                    Container container = containerRepo.findByNumberContainer(historyBox.get(0)).get();
                    messageToPeople("Ошибка! Короб " + box.getNumberBox() + " не найден в сборочном листе!");
                    saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Короб " + box.getNumberBox() + " не найдена в сборочном листе!" , LvlEvent.WARNING, macAddress);
                    playSound("Этот_товар_не_найден_в_сборочном_листе.wav");
                }
            }
        }
        else {
            messageToPeople("Ошибка! Штрихкод не распознан! " + bufferCode);
            saveLog(bufferCode, "Сборка короба " + cont.getNumberContainer() +". Считан неопознанный штрихкод", LvlEvent.WARNING, macAddress);
            playSound("Штрихкод_не_опознан.wav");
        }
    }
    // добавление новой марки в базу
    public void newMark(Container container, String numberBox, ContainerContent containerContent) {
        ContainerBox contBox = contBoxRepo.findByNumberContainerAndNumberBox(container.getNumberContainer(), numberBox).orElse(new ContainerBox());
        if (contBox.getNumberBox() != null && !contBox.getNumberBox().isEmpty()) {
            messageToPeople("Ошибка! Короб " + numberBox + " уже есть в упаковке!");
            saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Короб уже есть в упаковке.", LvlEvent.WARNING, macAddress);
            playSound("Эта_марка_уже_лежит_в_коробе.wav");
        }
        else {
            if (Integer.parseInt(inBoxNow.getValue()) == 0) {
                inBoxNow.setValue("1");
            }
            else {
                inBoxNow.setValue(String.valueOf(Integer.parseInt(inBoxNow.getValue())+1));
            }
            containerContent.setCountNow(containerContent.getCountNow()+1);
            containerContent.setMacAddress(macAddress);
            contBox = new ContainerBox();
            contBox.setMacAddress(macAddress);
            contBox.setNumberBox(numberBox);
            contBox.setNumberContainer(container.getNumberContainer());
            contBoxRepo.save(contBox);
            contContentRepo.save(containerContent);
            saveLog(bufferCode, "Сборка упаковки " + historyBox.get(0) + ". Короб добавлен в упаковку.", LvlEvent.INFO, macAddress);
            playSound("Ок.wav");
            messageToPeople("Короб добавлен в упаковку");
            updateGrid();
        }
    }
    // обработка считывания пользователем штрихкода короба
    public void processBarcodeContainer () {
        transactionRepository.save(new Transaction(macAddress));
        saveLog("", "Транзакция открыта", LvlEvent.CRITICAL, macAddress);
        if (isStarted) {
            switch (historyBox.size()) {
                case 2: {
                    saveLog("", "Инициализация отмены сборки короба", LvlEvent.SYSTEM_INFO, macAddress);
                    startCancelBuildBox();
                    break;
                }
                case 3: {
                    saveLog("", "Отмена сборки короба", LvlEvent.SYSTEM_INFO, macAddress);
                    cancelBuildBox();
                }
            }
        }
        else {
            switch (historyBox.size()) {
                case 0: {
                    saveLog("", "Начало сборки короба", LvlEvent.SYSTEM_INFO, macAddress);
                    initBuildContainer();
                    break;
                }
                case 1: {
                    saveLog("", "Загрузка коробов", LvlEvent.SYSTEM_INFO, macAddress);
                    startBuildContainer();
                }
            }
        }
        try{
            transactionRepository.delete(transactionRepository.findBySession(macAddress));
            saveLog("", "Транзакция закрыта", LvlEvent.CRITICAL, macAddress);
        }
        catch (IncorrectResultSizeDataAccessException e1) {
            messageToPeople("Во время работы возникла ошибка! Обратитесь к системному администратору!");
            playSound("Ошибка.wav");
            saveLog(bufferCode, "Ошибка в транзакции", LvlEvent.WARNING, macAddress);
            transactionRepository.deleteAll(transactionRepository.findBySessions(macAddress));
        }
    }
    // старт сборки короба, подгрузка всех штрихкодов
    public void startBuildContainer () {
        if (bufferCode.equals(historyBox.get(0))) {
            Container container = containerRepo.findByNumberContainer(bufferCode).get();
            String statusCont = container.getStatus();
            if (statusCont.equals("В сборке")) {
                List<ContainerContent> cont = contContentRepo.findByNumberContainer(bufferCode);
                if (cont.size() > 0) {
                    String macAddressAFewTimes = cont.get(0).getMacAddress();
                    if (macAddressAFewTimes.equals(macAddress)) {
                        backToSession();
                        messageToPeople("Сборка короба будет продолжена");
                        saveLog(bufferCode, "Сборка короба " + container.getNumberContainer() + " восстановлена", LvlEvent.INFO, macAddress);
                        isStarted = true;
                        historyBox.add(bufferCode);
                    }
                    else {
                        errorMsgContAssembledInAnotherPC(container);
                    }
                }
                else {
                    errorMsgContExerciseDontFind();
                }

            }
            else if (statusCont.equals("Собран")){
                errorMsgContAssembled();
            }
            else {
                initialAssemblyContainer(container);
            }
        }
        else {
            errorMsgContScanQRAnoterCont();
        }
    }
    // начало сборки короба
    public void initialAssemblyContainer (Container cont) {
        setupGrid();
        historyBox.add(bufferCode);
        inBoxNow.setValue("0");
        okMsgContBuildWasStarted();
        cont = containerRepo.findByNumberContainer(bufferCode).get();
        cont.setStatus("В сборке");
        containerRepo.save(cont);
        isStarted = true;
    }
    // восстановление потеряной сессии
    public void backToSession() {
        List<ContainerContent> contContentList = contContentRepo.findByMacAddress(macAddress);
        List<ContainerBox> contBoxList = contBoxRepo.findByMacAddress(macAddress);
        if (contContentList.size() != 0) {
            Container cont = containerRepo.findByNumberContainer(bufferCode).orElse(new Container());
            if (!cont.getNumberContainer().isEmpty()) {
                inBoxNeed.setValue(String.valueOf(cont.getVariantContainer().getCountInBox()));
                grid.setItems(contContentList);
                if (contBoxList.size() != 0) {
                    inBoxNow.setValue(String.valueOf(contBoxList.size()));
                }
                else {
                    inBoxNow.setValue("0");
                }
            }
            else {
                errorMsgContDontFind();
            }
            numberBox.setValue(bufferCode);
        }
        else {
            setupGrid();
        }
    }
    // первоначальная загрузка списка товаров для сборки короба
    public void setupGrid () {
        Container cont = containerRepo.findByNumberContainer(bufferCode).orElse(new Container());
        if (!cont.getNumberContainer().isEmpty()) {
            VariantContainer variantCont = cont.getVariantContainer();
            List<DescriptionContainer> descriptionCont = descriptionContRepo.findByVariantContainer(variantCont);
            List<ContainerContent> contContents = new LinkedList<>();
            if (descriptionCont.size() > 0) {
                for (DescriptionContainer desc_cont : descriptionCont) {
                    ContainerContent contContent = new ContainerContent();
                    contContent.setMacAddress(macAddress);
                    contContent.setCountNeed(desc_cont.getCount());
                    contContent.setCountNow(0);
                    contContent.setNumberContainer(cont.getNumberContainer());
                    contContent.setNumberVariantBox(desc_cont.getNumberVariantBox());
                    contContents.add(contContent);
                }
                inBoxNeed.setValue(String.valueOf(variantCont.getCountInBox()));
                numberBox.setValue(bufferCode);
                contContentRepo.saveAll(contContents);
                updateGrid();
            }
            else {
                errorMsgContExerciseDontFind();
            }
        }
        else {
            errorMsgContDontFind();
        }
    }
    // загрузка товаров для сборки из базы
    public void updateGrid() {
        grid.setItems(contContentRepo.findByMacAddress(macAddress));
    }
    // инициализация отмены сборки короба
    public void startCancelBuildBox() {
        if (historyBox.get(0).equals(bufferCode)) {
            messageToPeople("Вы хотите прервать сборку короба?\r\nСчитайте штрихкод ещё раз");
            saveLog(bufferCode,"Сборка короба " + historyBox.get(0) +  ". Считан штрихкод короба. Инициация прерывания сборки короба", LvlEvent.INFO, macAddress);
            playSound("Если_хотите_отменить_сборку_короба_отсканируйте_штрихкод_короба_еще_раз.wav");
            historyBox.add(bufferCode);
        }
        else {
            errorMsgContScanQRAnoterCont();
        }
    }
    // отмена сборки корода
    public void cancelBuildBox() {
        if (historyBox.get(historyBox.size()-1).equals(bufferCode)) {
            Container cont = containerRepo.findByNumberContainer(numberBox.getValue()).orElse(new Container());
            if (!cont.getNumberContainer().isEmpty()) {
                cont.setStatus("");
                containerRepo.save(cont);
            }
            messageToPeople("Сборка короба отменена");
            saveLog(bufferCode, "Сборка короба " + historyBox.get(0) +" отменена", LvlEvent.INFO, macAddress);
            playSound("Сборка_короба_отменена._Уберите_товары_из_короба.wav");
            clearHistory();
        }
        else {
            if (historyBox.size() == 3) {
                historyBox.remove(2);
            }
            errorMsgContScanQRAnoterCont();
        }
    }
    // очистка временных таблиц
    private void clearHistory () {
        numberBox.clear();
        inBoxNeed.clear();
        inBoxNow.clear();
        contContentRepo.deleteByMacAddress(macAddress);
        contBoxRepo.deleteByMacAddress(macAddress);
        updateGrid();
        historyBox = new ArrayList<>();
        isStarted = false;
        firtsCheck = false;
    }
    // вывод сообщений пользователю о том, что он считал штрихкод не того короба
    public void errorScanBox() {
        if (isStarted) {
            messageToPeople("Ошибка! Штрихкод не распознан! " + bufferCode);
            saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Считан неопознанный штрихкод", LvlEvent.WARNING, macAddress);
            playSound("Штрихкод_не_опознан.wav");
        }
        else {
            messageToPeople("Ошибка! Штрихкод не распознан! " + bufferCode);
            saveLog(bufferCode, "Сборка короба не запущена. Считан неопознанный штрихкод", LvlEvent.WARNING, macAddress);
            playSound("Штрихкод_не_опознан.wav");
        }

    }
    // проверка состояния базы
    public boolean checkDBState() {
        List<StateDB> state = stateDBRepository.findAllSortByIdDesc();
        if (state.size() > 0) {
            StateDB currentState = state.get(0);
            if (!currentState.isLock()) {
                return false;
            }
            else {
                return true;
            }
        }
        else {
            return false;
        }
    }
    // сообщение о том, что база заблокирована
    public void okMsgDBIsLock() {
        List<StateDB> state = stateDBRepository.findAllSortByIdDesc();
        if (state.size() > 0) {
            StateDB currentState = state.get(0);
            if (currentState.getDescription().isEmpty()) {
                messageToPeople("База заблокирована. " + currentState.getDescription());
            }
            else {
                messageToPeople(currentState.getDescription());
            }
            saveLog(bufferCode, "Считан штрихкод. База заблокирована", LvlEvent.INFO, macAddress);
        }
    }
    // инициализация сборки короба
    public void initBuildContainer () {
        historyBox.add(bufferCode);
        List<ContainerContent> contContent = contContentRepo.findByMacAddress(macAddress);
        if (contContent == null || contContent.size() == 0) {
            Container cont = containerRepo.findByNumberContainer(bufferCode).orElse(new Container());
            if (cont.getNumberContainer() != null && !cont.getNumberContainer().isEmpty()) {
                if (cont.getStatus().equals("Собран")) {
                    errorMsgContAssembled();
                }
                else {
                    okMsgContStepOne();
                }
            }
            else {
                errorMsgContStepOne();
            }
        }
        else {
            if (bufferCode.equals(contContent.get(0).getNumberContainer())) {
                okMsgContStepOne();
            }
            else {
                messageToPeople("Вы уже собираете другой короб! Номер короба: " + contContent.get(0).getNumberContainer());
                saveLog(bufferCode, "Попытка начать сборку другого короба. " +
                                "Номер нового короба " + bufferCode + ". " +
                                "Номер собираемого короба " + contContent.get(0).getNumberContainer(),
                        LvlEvent.WARNING, macAddress);
                playSound("Ошибка.wav");
                historyBox = new LinkedList<>();
                historyBox.add(contContent.get(0).getNumberContainer());
            }
        }
    }
    // Для начала сборки необходимо отсканировать штрихкод короба!
    public void errorMsgBoxAttentionBuildDontStart () {
        messageToPeople("Внимание!\r\nДля начала сборки необходимо отсканировать штрихкод короба!");
        saveLog(bufferCode, "Считан штрихкод марки. Сборка короба ещё не начата.", LvlEvent.WARNING, macAddress);
        playSound("Перед_сканированием_марки_начните_сборку_короба.wav");
    }
    // штрихкод упаковки не распознан
    public void errorMsgContQrDontFind() {
        messageToPeople("Считан неопознанный штрихкод!" + bufferCode);
        saveLog(bufferCode, "Считанный штрихкод не опозан", LvlEvent.INFO, macAddress);
        playSound("Штрихкод_не_опознан.wav");
        bufferCode = "";
    }
    // Считан неопознанный штрихкод. контейнер
    public void errorMsgContStepOne() {
        messageToPeople("Считан неопознанный штрихкод " + bufferCode);
        saveLog(bufferCode, "Считан неопознанный штрихкод ", LvlEvent.INFO, macAddress);
        playSound("Штрихкод_не_опознан.wav");
        historyBox = new LinkedList<>();
    }
    // Сборка короба запущена на другом компьютере
    public void errorMsgContAssembledInAnotherPC(Container container) {
        messageToPeople("Сборка короба запущена на другом компьютере");
        saveLog(bufferCode, "Сборка короба " + container.getNumberContainer() + " запущена на другом компьютере", LvlEvent.INFO, macAddress);
        isStarted = true;
        historyBox = new LinkedList<>();
        playSound("Ошибка.wav");
    }
    // Задание на сборку для короба не найдено в базе данных!
    public void errorMsgContExerciseDontFind() {
        messageToPeople("Задание на сборку для короба " + historyBox.get(0) + " не найдено в базе данных!");
        saveLog(bufferCode, "Задание на сборку для короба " + historyBox.get(0) + " не найдено в базе данных!", LvlEvent.WARNING, macAddress);
        playSound("Ошибка.wav");
    }
    // Короб уже собран
    public void errorMsgContAssembled() {
        messageToPeople("Короб уже собран");
        saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + " уже выполнена", LvlEvent.WARNING, macAddress);
        playSound("Короб_уже_собран.wav");
        historyBox = new LinkedList<>();
    }
    // Сборка не запущена. Считан штрихкод другого короба
    public void errorMsgContScanQRAnoterCont() {
        messageToPeople("Ошибка! Считан неправильный штрихкод: " + bufferCode);
        saveLog(bufferCode,"Сборка не запущена. Считан другой штрихкод", LvlEvent.WARNING, macAddress);
        playSound("Ошибка.wav");
    }
    // Короб не найден!
    public void errorMsgContDontFind() {
        messageToPeople("Короб не найден!");
        saveLog(bufferCode, "Короб " + historyBox.get(0) + " не найден в базе данных", LvlEvent.WARNING, macAddress);
        playSound("Штрихкод_не_опознан.wav");
    }
    // первый шаг прошел успешно. короб ещё не собран.
    public void okMsgContStepOne() {
        messageToPeople("Отсканируйте штрихкод ещё раз");
        saveLog(bufferCode, "Инициация сборки короба.", LvlEvent.INFO, macAddress);
        playSound("Подтвердите_начало_сборки_короба_сканированием_штрихкода_короба_еще_раз.wav");
    }
    // Сборка короба начата
    public void okMsgContBuildWasStarted() {
        messageToPeople("Сборка короба начата");
        playSound("Сборка_короба_начата._Отсканируйте_товары_согласно_сборочного_листа.wav");
        saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + " начата.", LvlEvent.INFO, macAddress);
    }
}
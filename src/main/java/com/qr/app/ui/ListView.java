package com.qr.app.ui;

import com.qr.app.backend.Configurer;
import com.qr.app.backend.Noticer;
import com.qr.app.backend.entity.Good;
import com.qr.app.backend.entity.Mark;
import com.qr.app.backend.entity.db.StateDB;
import com.qr.app.backend.entity.db.Transaction;
import com.qr.app.backend.entity.forSession.LogSession;
import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.entity.forSession.temporarytable.box.BoxContent;
import com.qr.app.backend.entity.forSession.temporarytable.box.BoxMark;
import com.qr.app.backend.entity.order.box.Box;
import com.qr.app.backend.entity.order.box.DescriptionBox;
import com.qr.app.backend.entity.order.box.VariantBox;
import com.qr.app.backend.repository.GoodRepository;
import com.qr.app.backend.repository.LogSessionRepository;
import com.qr.app.backend.repository.MarkRepository;
import com.qr.app.backend.repository.db.StateDBRepository;
import com.qr.app.backend.repository.db.TransactionRepository;
import com.qr.app.backend.repository.order.box.BoxRepository;
import com.qr.app.backend.repository.order.box.DescriptionBoxRepository;
import com.qr.app.backend.repository.sound.SoundRepository;
import com.qr.app.backend.repository.temporary.box.BoxContentRepository;
import com.qr.app.backend.repository.temporary.box.BoxMarkRepository;
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
import jssc.SerialPortList;
import org.hibernate.NonUniqueResultException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.qr.app.backend.Player.playSound;

@Route(value = "")
@Push
public class ListView extends VerticalLayout {

    private Grid<BoxContent> grid;
    private Label numberBoxLabel = new Label("Номер короба: ");
    private Label inBoxNeedLabel = new Label("Надо: ");
    private Label inBoxNowLabel = new Label("Собрано: ");
    private TextField numberBox = new TextField();
    private TextField inBoxNeed = new TextField();
    private TextField inBoxNow = new TextField();

    private Dialog dialog = new Dialog();
    private String bufferCode;

    private boolean lock = false;

    private SerialPort serialPort;

    private boolean isStarted = false;
    private List<String> historyBox;
    private String macAddress;
    private boolean firtsCheck = false;

    private final BoxRepository boxRepository;
    private final DescriptionBoxRepository descriptionBoxRepository;
    private final GoodRepository goodRepository;
    private final MarkRepository markRepository;
    private final BoxContentRepository boxContentRepository;
    private final BoxMarkRepository boxMarkRepository;
    private final LogSessionRepository logSessionRepository;
    private final StateDBRepository stateDBRepository;
    private final TransactionRepository transactionRepository;

    public ListView (BoxRepository boxRepository, DescriptionBoxRepository descriptionBoxRepository, GoodRepository goodRepository, MarkRepository markRepository, BoxContentRepository boxContentRepository, BoxMarkRepository boxMarkRepository, LogSessionRepository logSessionRepository, StateDBRepository stateDBRepository, TransactionRepository transactionRepository) throws FileNotFoundException {
        this.boxRepository = boxRepository;
        this.descriptionBoxRepository = descriptionBoxRepository;
        this.goodRepository = goodRepository;
        this.markRepository = markRepository;
        this.boxContentRepository = boxContentRepository;
        this.boxMarkRepository = boxMarkRepository;
        this.logSessionRepository = logSessionRepository;
        this.stateDBRepository = stateDBRepository;
        this.transactionRepository = transactionRepository;

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

        logSessionRepository.save(new LogSession(new Date().getTime(), "", "Начало создания конструктора", LvlEvent.SYSTEM_INFO, "-"));

        String[] portNames = SerialPortList.getPortNames();

        for (int i = 0; i < portNames.length; i++) {
            System.out.println(portNames[i]);
        }

        serialPort = new SerialPort(Configurer.getDevPort());
        refresh:

        try {

            logSessionRepository.save(new LogSession(new Date().getTime(), "", "Попытка подключения к сканеру", LvlEvent.SYSTEM_INFO, "-"));

            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);

            logSessionRepository.save(new LogSession(new Date().getTime(), "", "Настройки подготовлены для сканера", LvlEvent.SYSTEM_INFO, "-"));

            serialPort.addEventListener(click -> {
                    logSessionRepository.save(new LogSession(new Date().getTime(), "", "На сканер пришла информация", LvlEvent.SYSTEM_INFO, "-"));
                    StringBuilder sb = new StringBuilder();
                    try {
                        sb.append(serialPort.readString(click.getEventValue()));
                        UI ui = getUI().get();
                        ui.access(() -> {
                            String data = sb.toString();
                            if (data.length() <= 255) {
                                saveLog("", "Пришло " + data, LvlEvent.SYSTEM_INFO, macAddress);
                                int symbol = sb.indexOf("\r\n");
                                saveLog("", "Обработка " + symbol, LvlEvent.SYSTEM_INFO, macAddress);
                                if (symbol != -1){
                                    bufferCode = bufferCode.concat(data.substring(0, symbol));
                                    saveLog(bufferCode, "На сканер пришла информация", LvlEvent.SYSTEM_INFO, macAddress);
                                    analyseCode();
                                }
                                else {
                                    saveLog(bufferCode, "Сборка bufferCode " + data, LvlEvent.SYSTEM_INFO, macAddress);
                                    bufferCode += data;
                                }
                            }
                            else {
                                bufferCode = "";
                                messageToPeople(Noticer.readQrSomeSlowly());
                            }
                        });
                    } catch (SerialPortException e) {
                        saveLog("", "Ошибка. Управление потоком не передано", LvlEvent.SYSTEM_INFO, macAddress);
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
    // конфигурация текстовых полей
    public HorizontalLayout getToolBar () {
        HorizontalLayout layout = new HorizontalLayout(numberBoxLabel, numberBox, inBoxNeedLabel, inBoxNeed, inBoxNowLabel, inBoxNow);
        numberBox.setReadOnly(true);
        inBoxNeed.setReadOnly(true);
        inBoxNow.setReadOnly(true);
        return layout;
    }
    // конфигурация таблицы
    public void configureGrid () {
        grid = new Grid<>(BoxContent.class);
        grid.setSizeFull();
        grid.removeAllColumns();
        grid.addColumn(BoxContent::getArticle).setHeader("Артикул");
        grid.addColumn(BoxContent::getSize).setHeader("Размер");
        grid.addColumn(BoxContent::getColor).setHeader("Цвет");
        grid.addColumn(BoxContent::getCountNow).setHeader("Количество собрано");
        grid.addColumn(BoxContent::getCountNeed).setHeader("Количество надо");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }
    // анализ штрихкодов
    public void analyseCode() {

        if (bufferCode.length() == 18) {
            saveLog(bufferCode, "Обработка штрихкода", LvlEvent.CRITICAL, macAddress);
            List<StateDB> state = stateDBRepository.findAllSortByIdDesc();
            String firstSymbol = bufferCode.substring(0, 1);
            String fourthSymbol = bufferCode.substring(3, 4);
            if (state.size() > 0) {
                StateDB currentState = state.get(0);
                if (!currentState.isLock()) {
                    processBarcodeBox(firstSymbol, fourthSymbol);
                }
                else {
                    if (currentState.getDescription().isEmpty()) {
                        messageToPeople("База заблокирована. " + currentState.getDescription());
                    }
                    else {
                        messageToPeople(currentState.getDescription());
                    }
                    saveLog(bufferCode, "Считан штрихкод. База заблокирована", LvlEvent.INFO, macAddress);
                }
            }
            else {
                saveLog(bufferCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
                processBarcodeBox(firstSymbol, fourthSymbol);
            }

        }
        else if (bufferCode.length() > 31) {
            saveLog(bufferCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
            if (historyBox.size() == 3 ) {
                historyBox.remove(2);
            }

            String leftPart = bufferCode.substring(0, 10);
            if (leftPart.equals("0104680035") ||
                    leftPart.equals("0104680016") ||
                    leftPart.equals("0104610095")) {

                List<StateDB> state = stateDBRepository.findAllSortByIdDesc();

                if (state.size() > 0) {

                    StateDB currentState = state.get(0);

                    if (!currentState.isLock()) {
                        processBarcodeMark();
                    }
                    else {
                        messageToPeople("База заблокирована" + currentState.getDescription());
                        saveLog(bufferCode, "Считан штрихкод. База заблокирована", LvlEvent.INFO, macAddress);
                    }
                }
                else {
                    processBarcodeMark();
                }

            }
            else {
                messageToPeople("Считан неопознанный штрихкод! " + bufferCode);
                saveLog(bufferCode, "Считанный штрихкод не опозан", LvlEvent.INFO, macAddress);
                playSound("Штрихкод_не_опознан.wav");
                bufferCode = "";
            }

        }
        else {

            if (historyBox.size() == 3) {
                historyBox.remove(2);
            }

            messageToPeople("Считан неопознанный штрихкод!" + bufferCode);
            saveLog(bufferCode, "Считанный штрихкод не опозан", LvlEvent.INFO, macAddress);
            playSound("Штрихкод_не_опознан.wav");
            bufferCode = "";

        }

        lock = false;
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
    // первоначальная загрузка списка товаров для сборки короба
    public void setupGrid (String numberBoxStr) {

        Box box = boxRepository.findByNumberBox(numberBoxStr).orElse(new Box());

        if (!box.getNumberBox().isEmpty()) {
            VariantBox variantBox = box.getVariantBox();
            List<DescriptionBox> descriptionBox = descriptionBoxRepository.findByVariantBox(variantBox);
            List<BoxContent> boxContents = new LinkedList<>();

            if (descriptionBox.size() > 0) {
                for (DescriptionBox desc_box : descriptionBox) {
                    Good good = goodRepository.findByBarcode(desc_box.getBarcode()).get();
                    BoxContent boxContent = new BoxContent();
                    boxContent.setMacAddress(macAddress);
                    boxContent.setBarcode(good.getBarcode());
                    boxContent.setArticle(good.getArticle());
                    boxContent.setColor(good.getColor());
                    boxContent.setSize(good.getSize());
                    boxContent.setCountNeed(desc_box.getCount());
                    boxContent.setCountNow(0);
                    boxContent.setNumberBox(box.getNumberBox());
                    boxContents.add(boxContent);

                }

                inBoxNeed.setValue(String.valueOf(variantBox.getCountInBox()));
                numberBox.setValue(bufferCode);
                boxContentRepository.saveAll(boxContents);
                updateGrid();
            }
            else {
                messageToPeople("Задание на сборку не найдено!");
                saveLog(bufferCode, "Задание на сборку короба " + historyBox.get(0) + " не найдено в базе данных", LvlEvent.WARNING, macAddress);
                playSound("Ошибка.wav");
            }


        }
        else {
            messageToPeople("Короб не найден!");
            saveLog(bufferCode, "Короб " + historyBox.get(0) + " не найден в базе данных", LvlEvent.WARNING, macAddress);
            playSound("Штрихкод_не_опознан.wav");
        }


    }
    // восстановление потеряной сессии
    public void backToSession(String numberBoxStr) {
        List<BoxContent> boxContentList = boxContentRepository.findByMacAddress(macAddress);
        List<BoxMark> boxMarkList = boxMarkRepository.findByMacAddress(macAddress);
        if (boxContentList.size() != 0) {
            Box box = boxRepository.findByNumberBox(numberBoxStr).orElse(new Box());
            if (!box.getNumberBox().isEmpty()) {
                inBoxNeed.setValue(String.valueOf(box.getVariantBox().getCountInBox()));
                grid.setItems(boxContentList);
                if (boxMarkList.size() != 0) {
                    inBoxNow.setValue(String.valueOf(boxMarkList.size()));
                }
                else {
                    inBoxNow.setValue("0");
                }
            }
            else {
                messageToPeople("Короб не найден! " + numberBoxStr);
                saveLog(bufferCode, "Короб " + historyBox.get(0) + " не найден в базе данных", LvlEvent.WARNING, macAddress);
                playSound("Штрихкод_не_опознан.wav");
            }
            numberBox.setValue(bufferCode);
        }
        else {
            setupGrid(numberBoxStr);
        }
    }
    // загрузка товаров для сборки из базы
    public void updateGrid() {
        grid.setItems(boxContentRepository.findByMacAddress(macAddress));
    }
    // добавление марки в короб
    public void insertMark() {
        String cis = bufferCode.substring(0,31);
        Box box = boxRepository.findByNumberBox(historyBox.get(0)).orElse(new Box());
        Mark mark = markRepository.findByCis(cis).orElse(new Mark());
        if (mark.getCis() != null && !mark.getCis().isEmpty()) {
            if (mark.getNumberBox() != null && !mark.getNumberBox().isEmpty()) {
                messageToPeople("Ошибка! Данная марка находится в другом коробе!");
                saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Марка находится в другом коробе " + mark.getNumberBox() , LvlEvent.WARNING, macAddress);
                playSound("Эта_марка_уже_отгружена.wav");
            }
            else {
                BoxContent boxContent = boxContentRepository.findByBarcode(mark.getBarcode()).orElse(new BoxContent());
                if (boxContent.getBarcode() != null && !boxContent.getBarcode().isEmpty()) {
                    int nowCount = boxContent.getCountNow();
                    int needCount = boxContent.getCountNeed();
                    if (!box.getNumberBox().isEmpty()) {
                        if (needCount == 0) {
                            newMark(box, cis, boxContent);
                        }
                        else {
                            if (needCount > nowCount) {
                                newMark(box, cis, boxContent);
                            }
                            if (needCount == nowCount) {
                                Good good = goodRepository.findByBarcode(boxContent.getBarcode()).get();
                                messageToPeople("Ошибка! Отложите " + good.getName() + "! Сборка этой обуви закончена!");
                                saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Сборка данного вида обуви закончена!", LvlEvent.WARNING, macAddress);
                                playSound("Сборка_этого_товара_уже_закончена.wav");
                            }
                        }
                    }
                    else {
                        messageToPeople("Ошибка! Короб не найден! " + historyBox.get(0));
                        saveLog(bufferCode, "Короб " + box.getNumberBox() +" не найден в базе данных!", LvlEvent.WARNING, macAddress);
                        playSound("Штрихкод_не_опознан.wav");
                    }
                }
                else {
                    String nameShoes = goodRepository.findByBarcode(mark.getBarcode()).get().getName();
                    messageToPeople("Ошибка! Обувь " + nameShoes + " не найдена в сборочном листе!");
                    saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Обувь " +nameShoes + " не найдена в сборочном листе!" , LvlEvent.WARNING, macAddress);
                    playSound("Этот_товар_не_найден_в_сборочном_листе.wav");
                }

            }
        }
        else {
            messageToPeople("Ошибка! Штрихкод не распознан! " + bufferCode);
            saveLog(bufferCode, "Сборка короба " + box.getNumberBox() +". Считан неопознанный штрихкод", LvlEvent.WARNING, macAddress);
            playSound("Штрихкод_не_опознан.wav");
        }

    }
    // добавление новой марки в базу
    public void newMark(Box box, String cis, BoxContent markInJson) {
        BoxMark control = boxMarkRepository.findByNumberBoxAndCis(box.getNumberBox(), cis).orElse(new BoxMark());
        if (control.getCis() != null || !control.getCis().isEmpty()) {
            messageToPeople("Ошибка! Марка " + cis + " уже есть в коробе!");
            saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Марка уже есть в коробе.", LvlEvent.WARNING, macAddress);
            playSound("Эта_марка_уже_лежит_в_коробе.wav");
        }
        else {
            if (Integer.parseInt(inBoxNow.getValue()) == 0) {
                inBoxNow.setValue("1");
            }
            else {
                inBoxNow.setValue(String.valueOf(Integer.parseInt(inBoxNow.getValue())+1));
            }
            markInJson.setCountNow(markInJson.getCountNow()+1);
            markInJson.setMacAddress(macAddress);
            control = new BoxMark();
            control.setCis(cis);
            control.setNumberBox(box.getNumberBox());
            control.setMacAddress(macAddress);
            boxMarkRepository.save(control);
            boxContentRepository.save(markInJson);
            saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Марка добавлена в короб.", LvlEvent.INFO, macAddress);
            playSound("Ок.wav");
            messageToPeople("Марка добавлена в короб");
            updateGrid();
        }
    }
    // перенос данных из временных баз, в постоянные
    public void transferData() {

        saveLog("", "Начало переноса данных из временной базы в постоянную.", LvlEvent.SYSTEM_INFO, macAddress);
        List<BoxMark> boxMarks =  boxMarkRepository.findByMacAddress(macAddress);
        List<Mark> modifiedMarks = new LinkedList<>();
        Box box = boxRepository.findByNumberBox(boxMarks.get(0).getNumberBox()).get();
        box.setStatus("Собран");

        for (BoxMark boxMark : boxMarks) {

            String cis = boxMark.getCis();
            String numberBox = boxMark.getNumberBox();
            Mark mark = markRepository.findByCis(cis).orElse(new Mark());

            if (mark.getCis() != null && !mark.getCis().isEmpty()) {
                mark.setDate(new Date().getTime());
                mark.setNumberBox(numberBox);
                modifiedMarks.add(mark);
            }
            else {
                saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Марка " + cis + " не найдена в базе данных", LvlEvent.WARNING, macAddress);
            }

        }

        markRepository.saveAll(modifiedMarks);
        boxRepository.save(box);

        saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + " завершена", LvlEvent.INFO, macAddress);

    }
    // запись событий в базу данных
    public void saveLog (String bufferCode, String descriptionEvent, LvlEvent lvlEvent, String macAddress) {
        LogSession event = new LogSession(new Date().getTime(), bufferCode, descriptionEvent, lvlEvent, macAddress);
        logSessionRepository.save(event);
    }
    // начало сборки короба
    public void initialAssemblyBox(Box box, String message) {
        setupGrid(bufferCode);
        historyBox.add(bufferCode);
        inBoxNow.setValue("0");
        messageToPeople("Сборка короба начата");
        playSound("Сборка_короба_начата._Отсканируйте_товары_согласно_сборочного_листа.wav");
        saveLog(bufferCode, message, LvlEvent.INFO, macAddress);
        box = boxRepository.findByNumberBox(bufferCode).get();
        box.setStatus("В сборке");
        boxRepository.save(box);
        isStarted = true;
    }
    // очистка временных таблиц
    private void clearHistory () {
        numberBox.clear();
        inBoxNeed.clear();
        inBoxNow.clear();
        boxContentRepository.deleteByMacAddress(macAddress);
        boxMarkRepository.deleteByMacAddress(macAddress);
        updateGrid();
        historyBox = new ArrayList<>();
        isStarted = false;
        firtsCheck = false;
    }
    // обработка считывания пользователем штрихкода короба
    public void processBarcodeBox(String firstSymbol, String fourthSymbol) {

        transactionRepository.save(new Transaction(macAddress));
        saveLog("", "Транзакция открыта", LvlEvent.CRITICAL, macAddress);

        if (firstSymbol.equals("0") && firstSymbol.equals(fourthSymbol)) {
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
                        initBuildBox();
                        break;
                    }
                    case 1: {
                        saveLog("", "Загрузка товаров", LvlEvent.SYSTEM_INFO, macAddress);
                        startBuildBox();
                    }
                }
            }
        }
        else {
            errorScanBox();
        }

        try{
            transactionRepository.delete(transactionRepository.findBySession(macAddress));
            saveLog("", "Транзакция закрыта", LvlEvent.CRITICAL, macAddress);
        }
        catch (NonUniqueResultException e) {
            messageToPeople("Во время работы возникла ошибка! Обратитесь к системному администратору!");
            playSound("Ошибка.wav");
            saveLog(bufferCode, "Ошибка в транзакции", LvlEvent.WARNING, macAddress);
        }

    }
    // старт сборки короба, подгрузка всех штрихкодов
    public void startBuildBox() {

        if (bufferCode.equals(historyBox.get(0))) {

            Box box = boxRepository.findByNumberBox(bufferCode).get();

            String statusBox = box.getStatus();

            if (statusBox.equals("В сборке")) {

                List<BoxContent> boxes = boxContentRepository.findByNumberBox(bufferCode);
                if (boxes.size() > 0) {
                    String macAddressAFewTimes = boxes.get(0).getMacAddress();
                    if (macAddressAFewTimes.equals(macAddress)) {
                        backToSession(box.getNumberBox());
                        messageToPeople("Сборка короба будет продолжена");
                        saveLog(bufferCode, "Сборка короба " + box.getNumberBox() + " восстановлена", LvlEvent.INFO, macAddress);
                        isStarted = true;
                        historyBox.add(bufferCode);
                    }
                    else {
                        messageToPeople("Сборка короба запущена на другом компьютере");
                        saveLog(bufferCode, "Сборка короба " + box.getNumberBox() + " запущена на другом компьютере", LvlEvent.INFO, macAddress);
                        isStarted = true;
                        historyBox = new LinkedList<>();
                        playSound("Ошибка.wav");
                    }
                }
                else {
                    messageToPeople("Задание на сборку для короба " + historyBox.get(0) + " не найдено в базе данных!");
                    saveLog(bufferCode, "Задание на сборку для короба " + historyBox.get(0) + " не найдено в базе данных!", LvlEvent.WARNING, macAddress);
                    playSound("Ошибка.wav");
                }

            }
            else if (statusBox.equals("Собран")){
                messageToPeople("Короб уже собран");
                saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + " уже выполнена", LvlEvent.WARNING, macAddress);
                playSound("Короб_уже_собран.wav");
                historyBox = new LinkedList<>();
            }
            else {
                initialAssemblyBox(box, "Сборка короба " + historyBox.get(0) + " начата.");
            }


        }
        else {
            messageToPeople("Ошибка! Считан неправильный штрихкод: " + bufferCode);
            saveLog(bufferCode,"Сборка не запущена. Считан другой штрихкод", LvlEvent.WARNING, macAddress);
            playSound("Ошибка.wav");
        }

    }
    // инициализация сборки короба
    public void initBuildBox() {

        historyBox.add(bufferCode);
        List<BoxContent> boxContent = boxContentRepository.findByMacAddress(macAddress);

        if (boxContent == null || boxContent.size() ==0) {
            Box box = boxRepository.findByNumberBox(bufferCode).orElse(new Box());
            if (box.getNumberBox() != null && !box.getNumberBox().isEmpty()) {
                if (box.getStatus().equals("Собран")) {
                    messageToPeople("Короб уже собран");
                    saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + " уже выполнена", LvlEvent.WARNING, macAddress);
                    playSound("Короб_уже_собран.wav");
                    historyBox = new LinkedList<>();
                }
                else {
                    messageToPeople("Отсканируйте штрихкод ещё раз");
                    saveLog(bufferCode, "Инициация сборки короба.", LvlEvent.INFO, macAddress);
                    playSound("Подтвердите_начало_сборки_короба_сканированием_штрихкода_короба_еще_раз.wav");
                }
            }
            else {
                messageToPeople("Считан неопознанный штрихкод " + bufferCode);
                saveLog(bufferCode, "Считанный штрихкод не опозан", LvlEvent.INFO, macAddress);
                playSound("Штрихкод_не_опознан.wav");
                bufferCode = "";
                historyBox = new LinkedList<>();
            }
        }
        else {
            if (bufferCode.equals(boxContent.get(0).getNumberBox())) {
                messageToPeople("Отсканируйте штрихкод ещё раз");
                saveLog(bufferCode, "Инициация сборки короба.", LvlEvent.INFO, macAddress);
                playSound("Подтвердите_начало_сборки_короба_сканированием_штрихкода_короба_еще_раз.wav");
            }
            else {
                messageToPeople("Вы уже собираете другой короб! Номер короба: " + boxContent.get(0).getNumberBox());
                saveLog(bufferCode, "Попытка начать сборку другого короба. " +
                        "Номер нового короба " + bufferCode + ". " +
                        "Номер собираемого короба " + boxContent.get(0).getNumberBox(),
                        LvlEvent.WARNING, macAddress);
                playSound("Ошибка.wav");
                historyBox = new LinkedList<>();
                historyBox.add(boxContent.get(0).getNumberBox());
            }
        }

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
            messageToPeople("Внимание! Считан штрихкод другого короба: " + bufferCode);
            saveLog(bufferCode,"Сборка короба " + historyBox.get(0) + ". Считан штрихкод другого короба.", LvlEvent.WARNING, macAddress);
            playSound("Ошибка.wav");
        }
    }
    // отмена сборки корода
    public void cancelBuildBox() {

        if (historyBox.get(historyBox.size()-1).equals(bufferCode)) {

            Box box = boxRepository.findByNumberBox(numberBox.getValue()).orElse(new Box());
            if (!box.getNumberBox().isEmpty()) {
                box.setStatus("");
                boxRepository.save(box);
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
            messageToPeople("Внимание! Считан штрихкод другого короба: " + bufferCode);
            saveLog(bufferCode,"Сборка короба " + historyBox.get(0) + ". Считан штрихкод другого короба.", LvlEvent.WARNING, macAddress);
            playSound("Отмена_сборки_не_выполнена._Отсканирован_другой_штрихкод_короба.wav");
        }

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
    // обработка считывания штрихкода марки
    public void processBarcodeMark() {

        transactionRepository.save(new Transaction(macAddress));
        saveLog("", "Транзакция открыта", LvlEvent.CRITICAL, macAddress);

        if (isStarted) {

            if (firtsCheck) {
                saveLog(bufferCode, "Добавление марки в короб", LvlEvent.SYSTEM_INFO, macAddress);
                assemblyBox();
            }
            else {
                saveLog(bufferCode, "Добавление марки в короб. Первое сканирование", LvlEvent.SYSTEM_INFO, macAddress);
                firstScanMark();
                firtsCheck = true;
            }

        }
        else {
            messageToPeople("Внимание!\r\nДля начала сборки необходимо отсканировать штрихкод короба!");
            saveLog(bufferCode, "Считан штрихкод марки. Сборка короба ещё не начата.", LvlEvent.WARNING, macAddress);
            playSound("Перед_сканированием_марки_начните_сборку_короба.wav");
        }

        transactionRepository.delete(transactionRepository.findBySession(macAddress));
        saveLog("", "Транзакция закрыта", LvlEvent.CRITICAL, macAddress);

    }
    // обработка первого сканирования марки пользователем
    public void firstScanMark() {
        Box box = boxRepository.findByNumberBox(historyBox.get(0)).orElse(new Box());

        if (box.getNumberBox() != null && !box.getNumberBox().isEmpty()) {
            String statusBox = box.getStatus();

            if (statusBox.equals("В сборке")) {

                List<BoxContent> boxContent = boxContentRepository.findByMacAddress(macAddress);

                if (boxContent.size() > 0) {
                    assemblyBox();
                }
                else {
                    String mac = boxMarkRepository.findByNumberBoxAndCis(box.getNumberBox(), bufferCode.substring(0, 31)).get().getMacAddress();
                    messageToPeople("Ошибка! Сборка короба " + box.getNumberBox() +" уже начата на другом компьютере!" + mac);
                    saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + " уже выполняется на " + mac, LvlEvent.WARNING, macAddress);
                    playSound("Ошибка.wav");
                }

            }
            else if (statusBox.equals("Собран")){
                messageToPeople("Короб уже собран");
                saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + " уже выполнена", LvlEvent.WARNING, macAddress);
                playSound("Короб_уже_собран.wav");
            }
            else {
                assemblyBox();
            }
        }
        else {
            messageToPeople("Короб " + historyBox.get(0) + " не найден в базе данных!");
            saveLog(bufferCode, "Короб " + historyBox.get(0) + " не найден в базе данных", LvlEvent.WARNING, macAddress);
            playSound("Штрихкод_не_опознан.wav");
        }

    }
    // сборка короба
    public void assemblyBox() {
        int need = Integer.parseInt(inBoxNeed.getValue());
        insertMark();
        int now = Integer.parseInt(inBoxNow.getValue());
        saveLog("", "Сборка короба. Нужно собрать " + need + ". Собрано " + now, LvlEvent.SYSTEM_INFO, macAddress);
        if (need - now == 0) {
            transferData();
            clearHistory();
            messageToPeople("Сборка короба завершена!");
            playSound("Короб_собран.wav");
        }
    }

}
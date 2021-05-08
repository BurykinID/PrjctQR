package com.qr.app.ui;

import com.qr.app.backend.*;
import com.qr.app.backend.entity.HierarchyOfBoxes;
import com.qr.app.backend.entity.db.Transaction;
import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.entity.forSession.temporarytable.container.ContainerBox;
import com.qr.app.backend.entity.forSession.temporarytable.container.ContainerContent;
import com.qr.app.backend.entity.order.box.Box;
import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.entity.order.container.DescriptionContainer;
import com.qr.app.backend.entity.order.container.VariantContainer;
import com.qr.app.backend.service.StateDBService;
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

import java.io.FileNotFoundException;
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

    private Player player;

    private Dialog dialog = new Dialog();
    private String readCode;

    private SerialPort serialPort;

    private boolean isStarted = false;
    private List<String> historyBox;
    private String macAddress;
    private boolean firstCheck = false;

    private BuilderContainer builderContainer;

    public ContainerView () throws FileNotFoundException {
        addClassName("mark-view");
        setSizeFull();
        configureGrid();
        add(getToolBar(), grid);
        initParameters();
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
                            readCode = new Scanner().buildQrCode(sb);
                            if (!readCode.isEmpty()) {
                                builderContainer.defineTypeOfReadQRCode(readCode);
                                if (!new StateDBService().getDbState().isLock()) {






                                    builderContainer.analyseReadQrCode(readCode);
                                }
                                else
                                    okMsgDBIsLock();
                            }
                            else {
                                Noticer.readQrSomeSlowly();
                            }
                        });
                    } catch (SerialPortException e) {
                        logService.saveLog("", "Ошибка считывания значения со сканера", LvlEvent.SYSTEM_INFO, macAddress);
                        messageToPeople("Ошибка считывания значения со сканера. Повторите считывание");
                    }
            }, SerialPort.MASK_RXCHAR);
        }
        catch (SerialPortException ex) {
            logService.saveLog("", "Порт занят.", LvlEvent.CRITICAL, macAddress);
            messageToPeople("Порт занят. Проверьте, что сканер использует COM порт указанный в настройках! Выключите приложения, которые используют сканер. Перезапустите приложение");
        }
    }

    private void initParameters () throws FileNotFoundException {
        player = new Player();
        builderContainer = new BuilderContainer();
        macAddress = Configurer.getMacAddress();
        readCode = "";
        historyBox = new ArrayList<>();
        serialPort = new SerialPort(Configurer.getDevPort());
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
    // вывод сообщений на экран
    public void messageToPeople (String message){
        dialog.close();
        if (!message.isEmpty()) {
            dialog = new Dialog();
            dialog.add(new Label(message));
            dialog.open();
        }
    }
    // обработка считывания штрихкода содержимого
    public void processBarcodeBox () {
        transactionRepository.save(new Transaction(macAddress));
        logService.saveLog(readCode, "Транзакция открыта", LvlEvent.CRITICAL, macAddress);
        if (isStarted) {
            if (firstCheck) {
                logService.saveLog(readCode, "Добавление короба в короб", LvlEvent.SYSTEM_INFO, macAddress);
                assemblyBox();
            }
            else {
                logService.saveLog(readCode, "Добавление короба в короб. Первое сканирование", LvlEvent.SYSTEM_INFO, macAddress);
                firstScanBox();
                firstCheck = true;
            }
        }
        else {
            messageToPeople(Noticer.errorMsgBoxAttentionBuildDontStart(readCode, macAddress));
        }
        transactionRepository.delete(transactionRepository.findBySession(macAddress));
        logService.saveLog(readCode, "Транзакция закрыта", LvlEvent.CRITICAL, macAddress);
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
            player.playSound("Короб_собран.wav");
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
        saveLog(readCode, "Сборка короба " + historyBox.get(0) + " завершена", LvlEvent.INFO, macAddress);
    }
    // добавление box в container
    public void insertBox () {
        // найти короб по numberbox
        // определить вариант короба
        // если в description container содержится вариант короба, то проверяю что короб не лежит в друго контейнере
        Box box = boxRepo.findByNumberBox(readCode).orElse(new Box());
        Container cont = containerRepo.findByNumberContainer(historyBox.get(0)).orElse(new Container());
        if (box.getStatus() != null && box.getNumberBox() != null && box.getStatus().equals("Собран") && ! box.getNumberBox().isEmpty()) {
            HierarchyOfBoxes hierarchyOfBoxes = hierarchyOfBoxesRepo.findByNumberBox(box.getNumberBox()).orElse(new HierarchyOfBoxes());
            ContainerBox contBox = contBoxRepo.findByNumberBox(box.getNumberBox()).orElse(new ContainerBox());
            if (hierarchyOfBoxes.getDate() != 0 || !contBox.getNumberBox().isEmpty()) {
                messageToPeople("Ошибка! Данный короб находится в другой упаковке!");
                saveLog(readCode, "Сборка упаковки " + historyBox.get(0) + ". Короб находится в другой упаковке" + hierarchyOfBoxes.getNumberContainer() + "/" + contBox.getNumberContainer(), LvlEvent.WARNING, macAddress);
                Player.playSound("Эта_марка_уже_отгружена.wav");
            }
            else {
                ContainerContent contContent = contContentRepo.findByNumberVariantBox(box.getVariantBox().getNumberVariant()).orElse(new ContainerContent());
                if (contContent.getNumberVariantBox() != null && !contContent.getNumberVariantBox().isEmpty()) {
                    int nowCount = contContent.getCountNow();
                    int needCount = contContent.getCountNeed();
                    if (!cont.getNumberContainer().isEmpty()) {
                        if (needCount == 0) {
                            newMark(cont, readCode, contContent);
                        }
                        else {
                            if (needCount > nowCount) {
                                newMark(cont, readCode, contContent);
                            }
                            if (needCount == nowCount) {
                                messageToPeople("Ошибка! Отложите " + box.getVariantBox().getNumberVariant() + "! Сборка этих коробов закончена!");
                                saveLog(readCode, "Сборка короба " + historyBox.get(0) + ". Сборка данного вида коробов закончена!", LvlEvent.WARNING, macAddress);
                                Player.playSound("Сборка_этого_товара_уже_закончена.wav");
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
                    saveLog(readCode, "Сборка короба " + historyBox.get(0) + ". Короб " + box.getNumberBox() + " не найдена в сборочном листе!" , LvlEvent.WARNING, macAddress);
                    Player.playSound("Этот_товар_не_найден_в_сборочном_листе.wav");
                }
            }
        }
        else {
            messageToPeople("Ошибка! Штрихкод не распознан! " + readCode);
            saveLog(readCode, "Сборка короба " + cont.getNumberContainer() +". Считан неопознанный штрихкод", LvlEvent.WARNING, macAddress);
            Player.playSound("Штрихкод_не_опознан.wav");
        }
    }
    // добавление новой марки в базу
    public void newMark(Container container, String numberBox, ContainerContent containerContent) {
        ContainerBox contBox = contBoxRepo.findByNumberContainerAndNumberBox(container.getNumberContainer(), numberBox).orElse(new ContainerBox());
        if (contBox.getNumberBox() != null && !contBox.getNumberBox().isEmpty()) {
            messageToPeople("Ошибка! Короб " + numberBox + " уже есть в упаковке!");
            saveLog(readCode, "Сборка короба " + historyBox.get(0) + ". Короб уже есть в упаковке.", LvlEvent.WARNING, macAddress);
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
            saveLog(readCode, "Сборка упаковки " + historyBox.get(0) + ". Короб добавлен в упаковку.", LvlEvent.INFO, macAddress);
            playSound("Ок.wav");
            messageToPeople("Короб добавлен в упаковку");
            updateGrid();
        }
    }
    // начало сборки короба
    public void initialAssemblyContainer (Container cont) {
        setupGrid();
        historyBox.add(readCode);
        inBoxNow.setValue("0");
        okMsgContBuildWasStarted();
        cont = containerRepo.findByNumberContainer(readCode).get();
        cont.setStatus("В сборке");
        containerRepo.save(cont);
        isStarted = true;
    }
    // восстановление потеряной сессии
    public void backToSession() {
        List<ContainerContent> contContentList = contContentRepo.findByMacAddress(macAddress);
        List<ContainerBox> contBoxList = contBoxRepo.findByMacAddress(macAddress);
        if (contContentList.size() != 0) {
            Container cont = containerRepo.findByNumberContainer(readCode).orElse(new Container());
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
            numberBox.setValue(readCode);
        }
        else {
            setupGrid();
        }
    }
    // инициализация отмены сборки короба
    public void startCancelBuildBox() {
        if (historyBox.get(0).equals(readCode)) {
            messageToPeople("Вы хотите прервать сборку короба?\r\nСчитайте штрихкод ещё раз");
            saveLog(readCode,"Сборка короба " + historyBox.get(0) +  ". Считан штрихкод короба. Инициация прерывания сборки короба", LvlEvent.INFO, macAddress);
            playSound("Если_хотите_отменить_сборку_короба_отсканируйте_штрихкод_короба_еще_раз.wav");
            historyBox.add(readCode);
        }
        else {
            errorMsgContScanQRAnotherCont();
        }
    }
    // отмена сборки корода
    public void cancelBuildBox() {
        if (historyBox.get(historyBox.size()-1).equals(readCode)) {
            Container cont = containerRepo.findByNumberContainer(numberBox.getValue()).orElse(new Container());
            if (!cont.getNumberContainer().isEmpty()) {
                cont.setStatus("");
                containerRepo.save(cont);
            }
            messageToPeople("Сборка короба отменена");
            saveLog(readCode, "Сборка короба " + historyBox.get(0) +" отменена", LvlEvent.INFO, macAddress);
            playSound("Сборка_короба_отменена._Уберите_товары_из_короба.wav");
            clearHistory();
        }
        else {
            if (historyBox.size() == 3) {
                historyBox.remove(2);
            }
            errorMsgContScanQRAnotherCont();
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
        firstCheck = false;
        builderContainer = new BuilderContainer();
    }
    // первоначальная загрузка списка товаров для сборки короба
    public void setupGrid () {
        Container cont = containerRepo.findByNumberContainer(readCode).orElse(new Container());
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
                numberBox.setValue(readCode);
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
    // инициализация сборки короба
    public void initBuildContainer () {
        //передать штрихкод на обработку
        BuilderContainer.confirmationOfContainerAssembly();
        messageToPeople();
    }
    // сообщение о том, что база заблокирована
    public void okMsgDBIsLock() {
        messageToPeople(Noticer.okMsgDBIsLock(readCode, macAddress));
    }
    // первый шаг прошел успешно. короб ещё не собран.
    public void okMsgContStepOne() {
        messageToPeople(Noticer.okMsgContStepOne(builderContainer));
    }
    // Сборка короба начата
    public void okMsgContBuildWasStarted() {
        messageToPeople(Noticer.okMsgContBuildWasStarted(builderContainer));
    }
    // штрихкод упаковки не распознан
    public void errorMsgContQrDontFind() {
        messageToPeople(Noticer.errorMsgContQrDontFind(builderContainer));
        readCode = "";
    }
    // Считан неопознанный штрихкод. контейнер
    public void errorMsgContStepOne() {
        messageToPeople(Noticer.errorMsgContStepOne(builderContainer));
        readCode = "";
    }
    // Сборка короба запущена на другом компьютере
    public void errorMsgContAssembledInAnotherPC(Container container) {
        messageToPeople(Noticer.errorMsgContAssembledInAnotherPC(builderContainer));
    }
    // Задание на сборку для короба не найдено в базе данных!
    public void errorMsgContExerciseDontFind() {
        messageToPeople(Noticer.errorMsgContExerciseDontFind(builderContainer));
    }
    // Короб уже собран
    public void errorMsgContAssembled() {
        messageToPeople(Noticer.errorMsgContAssembled(builderContainer));
        builderContainer = new BuilderContainer();
    }
    // Сборка не запущена. Считан штрихкод другого короба
    public void errorMsgContScanQRAnotherCont () {
        messageToPeople(Noticer.errorMsgContScanQRAnotherCont(builderContainer));
    }
    // Короб не найден!
    public void errorMsgContDontFind() {
        messageToPeople(Noticer.errorMsgContDontFind(builderContainer));
    }
}
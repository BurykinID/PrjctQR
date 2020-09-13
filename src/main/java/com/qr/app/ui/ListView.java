package com.qr.app.ui;

import com.qr.app.backend.entity.db.Transaction;
import com.qr.app.backend.entity.db.StateDB;
import com.qr.app.backend.entity.forSession.BoxMark;
import com.qr.app.backend.entity.forSession.LogSession;
import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.entity.forSession.BoxContent;
import com.qr.app.backend.entity.Good;
import com.qr.app.backend.entity.Mark;
import com.qr.app.backend.entity.order.Box;
import com.qr.app.backend.entity.order.DescriptionBox;
import com.qr.app.backend.entity.order.VariantBox;
import com.qr.app.backend.repository.*;
import com.qr.app.backend.repository.db.TransactionRepository;
import com.qr.app.backend.repository.db.StateDBRepository;
import com.qr.app.backend.repository.order.BoxRepository;
import com.qr.app.backend.repository.order.DescriptionBoxRepostitory;
import com.qr.app.backend.repository.sound.SoundRepository;
import com.qr.app.backend.repository.temporary.BoxContentRepository;
import com.qr.app.backend.repository.temporary.BoxMarkReposiotry;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

@Route(value = "")
@Push
public class ListView extends VerticalLayout {

    @Autowired
    private org.springframework.boot.ApplicationArguments applicationArguments;

    private Grid<BoxContent> grid;
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

    private final BoxRepository boxRepository;
    private final DescriptionBoxRepostitory descriptionBoxRepostitory;
    private final GoodRepository goodRepository;
    private final MarkRepository markRepository;
    private final BoxContentRepository boxContentRepository;
    private final BoxMarkReposiotry boxMarkRepository;
    private final LogSessionRepository logSessionRepository;
    private final StateDBRepository stateDBRepository;
    private final TransactionRepository transactionRepository;
    private final SoundRepository soundRepository;

    public ListView (BoxRepository boxRepository, DescriptionBoxRepostitory descriptionBoxRepostitory, GoodRepository goodRepository, MarkRepository markRepository, BoxContentRepository boxContentRepository, BoxMarkReposiotry boxMarkRepository, LogSessionRepository logSessionRepository, StateDBRepository stateDBRepository, TransactionRepository transactionRepository, SoundRepository soundRepository) {

        this.boxRepository = boxRepository;
        this.descriptionBoxRepostitory = descriptionBoxRepostitory;
        this.goodRepository = goodRepository;
        this.markRepository = markRepository;
        this.boxContentRepository = boxContentRepository;
        this.boxMarkRepository = boxMarkRepository;
        this.logSessionRepository = logSessionRepository;
        this.stateDBRepository = stateDBRepository;
        this.transactionRepository = transactionRepository;
        this.soundRepository = soundRepository;

        addClassName("mark-view");
        setSizeFull();
        configureGrid();

        add(getToolBar(), grid);

        macAddress = getMacAddress();

        bufferCode = "";
        historyBox = new ArrayList<>();

        serialPort = new SerialPort("COM3");
        refresh:
        try {

            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);

            serialPort.addEventListener(click -> {
                StringBuilder sb = new StringBuilder();
                try {
                    sb.append(serialPort.readString(click.getEventValue()));
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
                UI ui = getUI().get();
                ui.access(() -> {
                    String data = sb.toString();
                    int symbol = sb.indexOf("\r\n");
                    if (symbol != -1){
                        bufferCode = bufferCode.concat(data.substring(0, symbol));

                        if (bufferCode.length() == 18) {

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
                                processBarcodeBox(firstSymbol, fourthSymbol);
                            }

                        }
                        else if (bufferCode.length() > 31) {

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
							}

                        }
                        else {

                            if (historyBox.size() == 3) {
                                historyBox.remove(2);
                            }

                            messageToPeople("Считан неопознанный штрихкод!" + bufferCode);
                            saveLog(bufferCode, "Считанный штрихкод не опозан", LvlEvent.INFO, macAddress);
                            playSound("Штрихкод_не_опознан.wav");
                        }

                        bufferCode = "";
                    }
                    else {
                        bufferCode += data;
                    }
                });
            }, SerialPort.MASK_RXCHAR);
        }
        catch (SerialPortException ex) {
            saveLog(null, "Порт занят.", LvlEvent.CRITICAL, macAddress);
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
        grid.removeColumnByKey("barcode");
        grid.setColumns("article", "size", "color", "countNow", "countNeed");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
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
            List<DescriptionBox> descriptionBox = descriptionBoxRepostitory.findByVariantBox(variantBox);
            List<BoxContent> boxContents = new LinkedList<>();

            if (descriptionBox.size() > 0) {
                for (DescriptionBox desc_box : descriptionBox) {
                    Good good = goodRepository.findByBarcode(desc_box.getBarcode());
                    BoxContent boxContent = new BoxContent();
                    boxContent.setMacAddress(macAddress);
                    boxContent.setBarcode(good.getBarcode());
                    boxContent.setArticle(good.getArticle());
                    boxContent.setColor(good.getColor());
                    boxContent.setSize(good.getSize());
                    boxContent.setCountNeed(desc_box.getCount());
                    boxContent.setCountNow(0);
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
    public void backToSession(String numberBox) {
        List<BoxContent> boxContentList = boxContentRepository.findByMacAddress(macAddress);
        List<BoxMark> boxMarkList = boxMarkRepository.findByMacAddress(macAddress);
        if (boxContentList.size() != 0) {
            Box box = boxRepository.findByNumberBox(numberBox).orElse(new Box());

            if (!box.getNumberBox().isEmpty()) {
                inBoxNeed.setValue(String.valueOf(box.getVariantBox().getCountInBox()));
                grid.setItems(boxContentList);
                if (boxMarkList.size() != 0) {
                    inBoxNow.setValue(String.valueOf(boxMarkList.size()));
                }
            }
            else {
                messageToPeople("Короб не найден! " + numberBox);
                saveLog(bufferCode, "Короб " + historyBox.get(0) + " не найден в базе данных", LvlEvent.WARNING, macAddress);
                playSound("Штрихкод_не_опознан.wav");
            }


        }
        else {
            setupGrid(numberBox);
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
                                Good good = goodRepository.findByBarcode(boxContent.getBarcode());
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
                    String nameShoes = goodRepository.findByBarcode(mark.getBarcode()).getName();
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
        if (control.getCis() != null && !control.getCis().isEmpty()) {
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
    // получение mac адреса компьютера
    public String getMacAddress() {
        InetAddress ip = null;
        try {
            ip = InetAddress.getLocalHost();

            try {
                NetworkInterface network = NetworkInterface.getByInetAddress(ip);

                byte[] mac = network.getHardwareAddress();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length-1) ?  "-" : ""));
                }

                String macAddr = sb.toString();

                return macAddr;

            } catch (SocketException e) {
                e.printStackTrace();
            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;

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
                        startCancelBuildBox();
                        break;
                    }
                    case 3: {
                        cancelBuildBox();
                    }
                }
            }
            else {
                switch (historyBox.size()) {
                    case 0: {
                        initBuildBox();
                        break;
                    }
                    case 1: {
                        startBuildBox();
                    }
                }
            }
        }
        else {
            errorScanBox();
        }

        transactionRepository.delete(transactionRepository.findBySession(macAddress));
        saveLog("", "Транзакция закрыта", LvlEvent.CRITICAL, macAddress);

    }
    // старт сборки короба, подгрузка всех штрихкодов
    public void startBuildBox() {
        if (bufferCode.equals(historyBox.get(0))) {

            Box box = boxRepository.findByNumberBox(bufferCode).get();

            String statusBox = box.getStatus();

            if (statusBox.equals("В сборке")) {

                List<BoxMark> boxes = boxMarkRepository.findByNumberBox(bufferCode);
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
                        historyBox.add(bufferCode);
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
            historyBox = new LinkedList<>();

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
        if (historyBox.get(0).equals(bufferCode)) {

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
                assemblyBox();
            }
            else {
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
        int now = Integer.parseInt(inBoxNow.getValue());
        int need = Integer.parseInt(inBoxNeed.getValue());
        insertMark();
        if (need - now == 1) {
            transferData();
            clearHistory();
            messageToPeople("Сборка короба завершена!");
            playSound("Короб_собран.wav");
        }
    }
    // воспроизведение звука
    public void playSound(String nameSound) {
        com.qr.app.backend.entity.Sound forPlay = soundRepository.findByFilename(nameSound);

        //applicationArguments.getSourceArgs();
        // добавить создание пути по указанному агргументу

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

        sound.play();
        sound.join();
        sound.close();
        file.delete();

    }

}
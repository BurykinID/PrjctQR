package com.qr.app.ui;

import com.qr.app.backend.entity.forSession.BoxMark;
import com.qr.app.backend.entity.forSession.LogSession;
import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.entity.forSession.MarkView;
import com.qr.app.backend.entity.Good;
import com.qr.app.backend.entity.Mark;
import com.qr.app.backend.entity.box.Box;
import com.qr.app.backend.entity.box.DescriptionBox;
import com.qr.app.backend.entity.box.VariantBox;
import com.qr.app.backend.repository.*;
import com.qr.app.backend.repository.MarkViewRepository;
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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

@Route(value = "")
@Push
public class ListView extends VerticalLayout {

    private Grid<MarkView> grid;
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
    private final MarkViewRepository markViewRepository;
    private final BoxMarkReposiotry boxMarkRepository;
    private final EventSessionRepository eventSessionRepository;

    public ListView (BoxRepository boxRepository, DescriptionBoxRepostitory descriptionBoxRepostitory, GoodRepository goodRepository, MarkRepository markRepository, MarkViewRepository markViewRepository, BoxMarkReposiotry boxMarkRepository, EventSessionRepository eventSessionRepository) {

        this.boxRepository = boxRepository;
        this.descriptionBoxRepostitory = descriptionBoxRepostitory;
        this.goodRepository = goodRepository;
        this.markRepository = markRepository;
        this.markViewRepository = markViewRepository;
        this.boxMarkRepository = boxMarkRepository;
        this.eventSessionRepository = eventSessionRepository;

        addClassName("mark-view");
        setSizeFull();
        configureGrid();

        add(getToolBar(), grid);

        macAddress = getMacAddress();

        bufferCode = "";
        // история считанных коробов
        historyBox = new ArrayList<>();

        serialPort = new SerialPort("COM3");
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

                            String firstSymbol = bufferCode.substring(0, 1);
                            String fourthSymbol = bufferCode.substring(3, 4);

                            if (firstSymbol.equals("0") && firstSymbol.equals(fourthSymbol)) {
                                if (isStarted) {
                                    switch (historyBox.size()) {

                                        case 2: {

                                            if (historyBox.get(historyBox.size()-1).equals(bufferCode)) {
                                                messageToPeople("Вы хотите прервать сборку короба?\r\nСчитайте штрихкод ещё раз");
                                                saveLog(bufferCode,"Сборка короба " + historyBox.get(0) +  ". Считан штрихкод короба. Инициация прерывания сборки короба", LvlEvent.INFO, macAddress);
                                                historyBox.add(bufferCode);
                                            }
                                            else {
                                                messageToPeople("Внимание! Считан неопознанный штрихкод: " + bufferCode);
                                                saveLog(bufferCode,"Сборка короба " + historyBox.get(0) + ". Считан штрихкод другого короба.", LvlEvent.WARNING, macAddress);
                                            }
                                            break;

                                        }
                                        
                                        case 3: {
                                            messageToPeople("Сборка короба отменена");
                                            saveLog(bufferCode, "Сборка короба " + historyBox.get(0) +" отменена", LvlEvent.INFO, macAddress);
                                            clearHistory();
                                        }

                                    }
                                }
                                else {
                                    switch (historyBox.size()) {

                                        case 0: {
                                            messageToPeople("Отсканируйте штрихкод ещё раз");
                                            saveLog(bufferCode, "Инициация сборки короба.", LvlEvent.INFO, macAddress);
                                            historyBox.add(bufferCode);
                                            break;
                                        }

                                        case 1: {
                                            if (bufferCode.equals(historyBox.get(0))) {

                                                Box box = boxRepository.findByNumberBox(bufferCode);
                                                String statusBox = box.getStatus();
                                                if (statusBox.equals("В сборке")) {
                                                    String macAddressAFewTimes = boxMarkRepository.findByNumberBox(bufferCode).get(0).getMacAddress();
                                                    if (macAddressAFewTimes.equals(macAddress)) {
                                                        backToSession(box.getNumberBox());
                                                        saveLog(bufferCode, "Сборка короба " + box.getNumberBox() + " восстановлена", LvlEvent.INFO, macAddress);
                                                        isStarted = true;
                                                    }
                                                }
                                                else if (statusBox.equals("Собран")){
                                                    messageToPeople("Короб уже собран");
                                                    saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + " уже выполнена", LvlEvent.WARNING, macAddress);
                                                }
                                                else {
                                                    initialAssemblyBox(box, "Сборка короба " + historyBox.get(0) + " начата.");
                                                }

                                            }
                                            else {
                                                messageToPeople("Ошибка! Считан неправильный штрихкод: " + bufferCode);
                                                saveLog(bufferCode,"Сборка не запущена. Считан другой штрихкод", LvlEvent.WARNING, macAddress);
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                if (isStarted) {
                                    saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Считан неопознанный штрихкод", LvlEvent.WARNING, macAddress);
                                }
                                else {
                                    saveLog(bufferCode, "Сборка короба не запущена. Считан неопознанный штрихкод", LvlEvent.WARNING, macAddress);
                                }
                            }

                        }
                        else if (bufferCode.length() > 31) {
                            if (isStarted) {

                                if (firtsCheck) {
                                    assemblyBox();
                                }
                                else {
                                    Box box = boxRepository.findByNumberBox(historyBox.get(0));

                                    String statusBox = box.getStatus();

                                    if (statusBox.equals("В сборке")) {

                                        List<BoxMark> boxMarks = boxMarkRepository.findByNumberBox(box.getNumberBox());

                                        if (boxMarks.size() > 0) {
                                            String mac = boxMarks.get(0).getMacAddress();
                                            if (macAddress.equals(mac)) {
                                                // если мак адрес совпал, то не важно, он может продолжить сборку
                                                backToSession(box.getNumberBox());
                                                box.setStatus("В сборке");
                                                boxRepository.save(box);
                                                firtsCheck = true;
                                            }
                                            else {
                                                messageToPeople("Ошибка! Сборка короба уже начата на другом компьютере!");
                                                saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + " уже выполняется на " + mac, LvlEvent.WARNING, macAddress);
                                            }
                                        }
                                        else {
                                            assemblyBox();
                                        }
                                    }
                                    else if (statusBox.equals("Собран")){
                                        messageToPeople("Короб уже собран");
                                        saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + " уже выполнена", LvlEvent.WARNING, macAddress);
                                    }
                                    else {
                                        assemblyBox();
                                    }
                                    firtsCheck = true;
                                }

                            }
                            else {
                                messageToPeople("Внимание!\r\nДля начала сборки необходимо отсканировать штрихкод короба!");
                                saveLog(bufferCode, "Считан штрихкод марки. Сборка короба ещё не начата.", LvlEvent.WARNING, macAddress);
                            }

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
            System.out.println("Порт занят");
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
        grid = new Grid<>(MarkView.class);
        grid.setSizeFull();
        grid.removeColumnByKey("barcode");
        grid.setColumns("article", "size", "color", "countNow", "countNeed");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));

    }
    // вывод сообщений на экран
    public void messageToPeople (String message){

        dialog.close();
        dialog = new Dialog();
        dialog.add(new Label(message));
        dialog.open();

    }
    // первоначальная загрузка списка товаров для сборки короба
    public void setupGrid (String numberBoxStr) {

        Box box = boxRepository.findByNumberBox(numberBoxStr);

        VariantBox variantBox = box.getVariantBox();
        List<DescriptionBox> descriptionBox = descriptionBoxRepostitory.findByVariantBox(variantBox);
        List<MarkView> markViews = new LinkedList<>();

        for (DescriptionBox desc_box : descriptionBox) {
            Good good = goodRepository.findByBarcode(desc_box.getBarcode());
            MarkView markView = new MarkView();
            markView.setMacAddress(macAddress);
            markView.setBarcode(good.getBarcode());
            markView.setArticle(good.getArticle());
            markView.setColor(good.getColor());
            markView.setSize(good.getSize());
            markView.setCountNeed(desc_box.getCount());
            markView.setCountNow(0);
            markViews.add(markView);
        }

        inBoxNeed.setValue(String.valueOf(variantBox.getCountInBox()));
        numberBox.setValue(bufferCode);
        markViewRepository.saveAll(markViews);
        updateGrid();

    }
    // восстановление потеряной сессии
    public void backToSession(String numberBox) {
        List<MarkView> markViewList = markViewRepository.findByMacAddress(macAddress);
        List<BoxMark> boxMarkList = boxMarkRepository.findByMacAddress(macAddress);
        if (markViewList.size() != 0) {
            Box box = boxRepository.findByNumberBox(numberBox);
            inBoxNeed.setValue(String.valueOf(box.getVariantBox().getCountInBox()));
            grid.setItems(markViewList);
            if (boxMarkList.size() != 0) {
                inBoxNow.setValue(String.valueOf(boxMarkList.size()));
            }
        }
        else {
            setupGrid(numberBox);
        }
    }
    // загрузка товаров для сборки из базы
    public void updateGrid() {
        grid.setItems(markViewRepository.findByMacAddress(macAddress));
    }
    // добавление марки в короб
    public void insertMark() {
        String cis = bufferCode.substring(0,31);
        Box box = boxRepository.findByNumberBox(historyBox.get(0));
        Mark mark = markRepository.findByCis(cis);

        if (mark.getNumberBox() != null && !mark.getNumberBox().isEmpty()) {
            messageToPeople("Ошибка! Данная марка находится в другом коробе!");
            saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Марка находится в другом коробе " + mark.getNumberBox() , LvlEvent.WARNING, macAddress);
        }
        else {

            MarkView markInJson = markViewRepository.findByBarcode(mark.getBarcode());
            int nowCount = markInJson.getCountNow();
            int needCount = markInJson.getCountNeed();

            if (needCount == 0) {
                newMark(box, cis, markInJson);
            }
            else {
                if (needCount > nowCount) {
                    newMark(box, cis, markInJson);
                }
                if (needCount == nowCount) {
                    Good good = goodRepository.findByBarcode(markInJson.getBarcode());
                    messageToPeople("Ошибка! Отложите " + good.getName() + "! Сборка этой обуви закончена!");
                    saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Сборка данного вида обуви закончена!", LvlEvent.WARNING, macAddress);
                }
            }

        }
    }
    // добавление новой марки в базу
    public void newMark(Box box, String cis, MarkView markInJson) {
        BoxMark control = boxMarkRepository.findByNumberBoxAndCis(box.getNumberBox(), cis);
        if (control != null) {
            messageToPeople("Ошибка! Марка " + cis + " уже есть в коробе!");
            saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Марка уже есть в коробе.", LvlEvent.WARNING, macAddress);
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
            markViewRepository.save(markInJson);
            saveLog(bufferCode, "Сборка короба " + historyBox.get(0) + ". Марка добавлена в короб.", LvlEvent.INFO, macAddress);
            updateGrid();
        }
    }
    // перенос данных из временных баз, в постоянные
    public void transferData() {

        List<BoxMark> boxMarks =  boxMarkRepository.findByMacAddress(macAddress);
        List<Mark> modifiedMarks = new LinkedList<>();
        Box box = boxRepository.findByNumberBox(boxMarks.get(0).getNumberBox());

        if (box != null) {
            box.setStatus("Собран");
        }
        for (BoxMark boxMark : boxMarks) {

            String cis = boxMark.getCis();
            String numberBox = boxMark.getNumberBox();
            Mark mark = markRepository.findByCis(cis);

            if (mark != null) {
                mark.setDate(new Date().getTime());
                mark.setNumberBox(numberBox);
            }

            modifiedMarks.add(mark);

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
    // сборка короба
    public void assemblyBox() {
        int now = Integer.parseInt(inBoxNow.getValue());
        int need = Integer.parseInt(inBoxNeed.getValue());
        insertMark();
        if (need - now == 1) {
            transferData();
            markViewRepository.deleteByMacAddress(macAddress);
            boxMarkRepository.deleteByMacAddress(macAddress);
            isStarted = false;
            historyBox.clear();
            firtsCheck = false;
            messageToPeople("Сборка короба завершена!");
        }
    }
    // запись событий в базу данных
    public void saveLog (String bufferCode, String descriptionEvent, LvlEvent lvlEvent, String macAddress) {
        LogSession event = new LogSession(new Date().getTime(), bufferCode, descriptionEvent, lvlEvent, macAddress);
        eventSessionRepository.save(event);
    }
    // начало или восстановление сборки короба
    public void initialAssemblyBox(Box box, String message) {
        setupGrid(bufferCode);
        historyBox.add(bufferCode);
        inBoxNow.setValue("0");
        messageToPeople("Сборка короба начата");
        saveLog(bufferCode, message, LvlEvent.INFO, macAddress);
        box = boxRepository.findByNumberBox(bufferCode);
        box.setStatus("В сборке");
        boxRepository.save(box);
        isStarted = true;
    }
    // очистка временных таблиц
    private void clearHistory () {
        Box box = boxRepository.findByNumberBox(numberBox.getValue());
        box.setStatus("");
        boxRepository.save(box);
        numberBox.clear();
        inBoxNeed.clear();
        inBoxNow.clear();
        configureGrid();
        historyBox = new ArrayList<>();
        isStarted = false;
        firtsCheck = false;
        markViewRepository.deleteByMacAddress(macAddress);
        boxMarkRepository.deleteByMacAddress(macAddress);
    }

}
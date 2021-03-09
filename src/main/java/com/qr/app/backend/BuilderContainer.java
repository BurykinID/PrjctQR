package com.qr.app.backend;

import com.qr.app.backend.entity.db.Transaction;
import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.entity.forSession.temporarytable.container.ContainerContent;
import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.service.LogService;
import com.qr.app.backend.service.StateDBService;
import com.qr.app.backend.service.TransactionService;
import com.qr.app.ui.ContainerView;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.LinkedList;
import java.util.List;

import static com.qr.app.backend.service.LogService.saveLog;

@Getter
@Setter
public class BuilderContainer {

    private int countRepeatedQr;
    private String qr;
    private String macAddress;

    public BuilderContainer() {
        this.countRepeatedQr = 0;
        this.qr = "";
        this.macAddress = "";
    }

    public BuilderContainer (int countRepeatedQr, String qr, String macAddress) {
        this.countRepeatedQr = countRepeatedQr;
        this.qr = qr;
        this.macAddress = macAddress;
    }

    @Override
    public String toString () {
        return super.toString();
    }

    // анализ штрихкодов
    public void analyseCode(BuilderContainer builderContainer, String bufferCode) {
        if (bufferCode.length() == 18) {
            boolean lockDB = StateDBService.getDbState().isLock();
            LogService.saveLog(bufferCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
            if (!lockDB) {
                if (bufferCode.charAt(0) == '2') {// штрихкод упаковки
                    processBarcodeContainer(builderContainer, bufferCode);
                }
                else {// штрихкод содержимого
                    LogService.saveLog(bufferCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
                    if (builderContainer.getCountRepeatedQr() == 3)
                        builderContainer.setCountRepeatedQr(2);
                    processBarcodeBox();
                }
            }
            else {
                ContainerView containerView = new ContainerView();
                containerView.okMsgDBIsLock();
            }
        }
        else if (bufferCode.length() == 20) {
            boolean lockDB = StateDBService.getDbState().isLock();
            LogService.saveLog(bufferCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
            if (!lockDB) {
                if (bufferCode.charAt(2) == '2' && bufferCode.charAt(0) == '0' && bufferCode.charAt(0) == '0') {
                    processBarcodeContainer();
                }
                else {
                    LogService.saveLog(bufferCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
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
    }
    // обработка считывания пользователем штрихкода короба
    public BuilderContainer processBarcodeContainer (BuilderContainer currentBuilder, String bufferCode) {
        TransactionService.openTransaction(currentBuilder);
        saveLog("", "Транзакция открыта", LvlEvent.CRITICAL, macAddress);

        switch(currentBuilder.countRepeatedQr) {
            case 0:
                saveLog("", "Начало сборки короба", LvlEvent.SYSTEM_INFO, macAddress);
                startBuildContainer(currentBuilder, bufferCode);
                break;
            case 1:
                saveLog("", "Загрузка коробов", LvlEvent.SYSTEM_INFO, macAddress);
                confimationBuildContainer(currentBuilder, bufferCode);
                break;
            case 2:
                saveLog("", "Инициализация отмены сборки короба", LvlEvent.SYSTEM_INFO, macAddress);
                startCancelBuildBox();
                break;
            case 3:
                saveLog("", "Отмена сборки короба", LvlEvent.SYSTEM_INFO, macAddress);
                cancelBuildBox();
                break;
            default:

                break;
        }

        TransactionService.closeTransaction(currentBuilder);
        saveLog("", "Транзакция закрыта", LvlEvent.CRITICAL, macAddress);

    }

    public String startBuildContainer(BuilderContainer buildContainer, String bufferCode) {
        //Container container = ContainerService.getContainerByMac(macAddress);
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

    public String confimationBuildContainer(BuilderContainer builderContainer, String bufferCode) {

    }



}

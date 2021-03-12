package com.qr.app.backend;

import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.entity.forSession.temporarytable.container.ContainerContent;
import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.service.LogService;
import com.qr.app.backend.service.StateDBService;
import com.qr.app.backend.service.TransactionService;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

import static com.qr.app.backend.Player.playSound;

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
        boolean lockDB = new StateDBService().getDbState().isLock();
        if (!lockDB) {
            new LogService().saveLog(bufferCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
            if ((bufferCode.length() == 18 && bufferCode.charAt(0) == '2') ||
                (bufferCode.length() == 20 && bufferCode.charAt(0) == '0' && bufferCode.charAt(2) == '2')) {
                processBarcodeContainer(builderContainer, bufferCode);
            }
            else if (bufferCode.length() == 18 || bufferCode.length() == 20) {
                processBarcodeBox();
            }
            else {
                if (builderContainer.getCountRepeatedQr() == 3)
                    builderContainer.setCountRepeatedQr(2);
                errorMsgContQrDontFind();
            }
        }
        else {
            okMsgDBIsLock();
        }
    }
    // обработка считывания пользователем штрихкода короба
    public BuilderContainer processBarcodeContainer (BuilderContainer currentBuilder, String bufferCode) {
        TransactionService transactionService = new TransactionService();
        transactionService.openTransaction(currentBuilder);
        new LogService().saveLog("", "Транзакция открыта", LvlEvent.CRITICAL, macAddress);

        switch(currentBuilder.countRepeatedQr) {
            case 0:
                new LogService().saveLog("", "Начало сборки короба", LvlEvent.SYSTEM_INFO, macAddress);
                startBuildContainer(currentBuilder, bufferCode);
                break;
            case 1:
                new LogService().saveLog("", "Загрузка коробов", LvlEvent.SYSTEM_INFO, macAddress);
                confimationBuildContainer(currentBuilder, bufferCode);
                break;
            case 2:
                new LogService().saveLog("", "Инициализация отмены сборки короба", LvlEvent.SYSTEM_INFO, macAddress);
                startCancelBuildBox();
                break;
            case 3:
                new LogService().saveLog("", "Отмена сборки короба", LvlEvent.SYSTEM_INFO, macAddress);
                cancelBuildBox();
                break;
            default:

                break;
        }

        transactionService.closeTransaction(currentBuilder);
        new LogService().saveLog("", "Транзакция закрыта", LvlEvent.CRITICAL, macAddress);

    }
    //
    public String startBuildContainer(BuilderContainer buildContainer, String bufferCode) {
        //Container container = ContainerService.getContainerByMac(macAddress);
        if (buildContainer.getCountRepeatedQr() == 0) {
            buildContainer.setCountRepeatedQr(1);
        }
        else if (buildContainer.getQr().equals(bufferCode)){
            buildContainer.setCountRepeatedQr(buildContainer.getCountRepeatedQr()+1);
        }
        List<ContainerContent> contContent = contContentRepo.findByMacAddress(macAddress);
        if (contContent == null || contContent.size() == 0) {
            Container cont = containerRepo.findByNumberContainer(bufferCode).orElse(new Container());
            if (cont.getNumberContainer() != null && !cont.getNumberContainer().isEmpty()) {
                if (cont.getStatus().equals("Собран")) {
                    Noticer.errorMsgContAssembled();
                }
                else {
                    Noticer.okMsgContStepOne();
                }
            }
            else {
                Noticer.errorMsgContStepOne();
            }
        }
        else {
            if (bufferCode.equals(contContent.get(0).getNumberContainer())) {
                Noticer.okMsgContStepOne();
            }
            else {
                Noticer.errorMsgReductionAssemblyReadQrAnotherContainer();
                buildContainer.setQr(contContent.get(0).getNumberContainer());
                buildContainer.setCountRepeatedQr(1);
            }
        }
    }
    public String confimationBuildContainer(BuilderContainer builderContainer, String bufferCode) {

    }



}

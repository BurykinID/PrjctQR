package com.qr.app.backend;

import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.entity.forSession.temporarytable.container.ContainerContent;
import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.service.LogService;
import com.qr.app.backend.service.StateDBService;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BuilderContainer {

    private int countOfContainersQrRepeated;
    private String qr;
    private String macAddress;
    private TypeQR

    public BuilderContainer() {
        this.countOfContainersQrRepeated = 0;
        this.qr = "";
        this.macAddress = "";
    }

    public BuilderContainer (int countRepeatedQr, String qr, String macAddress) {
        this.countOfContainersQrRepeated = countRepeatedQr;
        this.qr = qr;
        this.macAddress = macAddress;
    }

    @Override
    public String toString () {
        return super.toString();
    }
    // анализ штрихкодов
    public void analyseReadQrCode (String readCode) {
        boolean lockDB = new StateDBService().getDbState().isLock();
        if (!lockDB) {
            new LogService().saveLog(readCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
            if ((readCode.length() == 18 && readCode.charAt(0) == '2') ||
                (readCode.length() == 20 && readCode.charAt(0) == '0' && readCode.charAt(2) == '2')) {
                readQrIsContainerBuildIt(readCode);
            }
            else if (readCode.length() == 18 || readCode.length() == 20) {
                readQrIsBoxBuildIt();
            }
            else {
                countOfContainersQrRepeated = countOfContainersQrRepeated == 3 ? 2 : countOfContainersQrRepeated;
                errorMsgContainerQrDontFind();
            }
        }
        else {
            okMsgDBIsLock();
        }
    }
    // обработка считывания пользователем штрихкода короба
    public BuilderContainer readQrIsContainerBuildIt (String readCode) {
        /*TransactionService transactionService = new TransactionService();
        transactionService.openTransaction(currentBuilder);
        new LogService().saveLog("", "Транзакция открыта", LvlEvent.CRITICAL, macAddress);*/
        switch (countOfContainersQrRepeated) {
            case 0:
                startOfContainerAssembly(readCode);
                break;
            case 1:
                confimationOfContainerAssembly(readCode);
                break;
            case 2:
                startCancelOfContainerAssembly();
                break;
            case 3:
                cancelOfContainerAssembly();
                break;
            default:
                break;
        }

        /*transactionService.closeTransaction(currentBuilder);
        new LogService().saveLog("", "Транзакция закрыта", LvlEvent.CRITICAL, macAddress);*/
    }
    //
    public String startOfContainerAssembly (String bufferCode) {
        countOfContainersQrRepeated = countOfContainersQrRepeated == 0 ? 1 : countOfContainersQrRepeated++;
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
    public String confimationOfContainerAssembly (String bufferCode) {

    }



}

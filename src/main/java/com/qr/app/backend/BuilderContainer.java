package com.qr.app.backend;

import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.entity.forSession.temporarytable.container.ContainerContent;
import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.service.ContainerService;
import com.qr.app.backend.service.LogService;
import com.qr.app.backend.service.StateDBService;
import com.qr.app.backend.status.ContainersState;
import com.qr.app.backend.status.ContainersStatusInDb;

import java.util.List;

public class BuilderContainer {

    private String qr;
    private int countOfContainersQrRepeated;
    private String macAddress;
    private ContainersState state;
    private ContainersStatusInDb statusInDb;

    public BuilderContainer() {
        this.countOfContainersQrRepeated = 0;
        this.macAddress = "";
        this.state = ContainersState.Initial;
        this.qr = "";
    }

    public BuilderContainer (String qr, int countRepeatedQr, String macAddress, ContainersState state, ContainersStatusInDb statusInDb) {
        this.qr = qr;
        this.countOfContainersQrRepeated = countRepeatedQr;
        this.macAddress = macAddress;
        this.state = state;
        this.statusInDb = statusInDb;
    }

    @Override
    public String toString () {
        return super.toString();
    }

    public void incrementCountOfContainersQrRepeated() {
        countOfContainersQrRepeated++;
    }

    public void updateStateOfContainer() {
        switch (countOfContainersQrRepeated) {
            case 0 : setState(ContainersState.Initial);
                break;
            case 1 : setState(ContainersState.StartOfAssembly);
                break;
            case 2 : setState(ContainersState.InAssembly);
                break;
            case 3 : setState(ContainersState.CancelAssembly);
                break;
        }
    }

    public void checkStatusOfContainerInDb() {
        ContainerService containerService = new ContainerService();
        statusInDb = containerService.getStatusContainer(qr);
    }

    // анализ штрихкодов
    public void analyseTypeReadQrCode(String readCode) {
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

    public int getCountOfContainersQrRepeated() {
        return countOfContainersQrRepeated;
    }

    public void setCountOfContainersQrRepeated(int countOfContainersQrRepeated) {
        this.countOfContainersQrRepeated = countOfContainersQrRepeated;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean getContainerAssembled() {
        return containerAssembled;
    }

    public void setContainerAssembled(boolean containerAssembled) {
        this.containerAssembled = containerAssembled;
    }

    public ContainersState getState() {
        return state;
    }

    public void setState(ContainersState state) {
        this.state = state;
    }

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public ContainersStatusInDb getStatusInDb() {
        return statusInDb;
    }

    public void setStatusInDb(ContainersStatusInDb statusInDb) {
        this.statusInDb = statusInDb;
    }
}

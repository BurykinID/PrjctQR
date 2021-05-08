package com.qr.app.backend;

import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.entity.forSession.temporarytable.container.ContainerContent;
import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.service.LogService;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BuilderContainer {

    private int countOfContainersQrRepeated;
    private String containerQr;
    private String qr;
    private String macAddress;
    private TypeQR typeOfLastReadQr;

    public BuilderContainer() {
        this.countOfContainersQrRepeated = 0;
        this.containerQr = "";
        this.macAddress = "";
        this.typeOfLastReadQr = TypeQR.NotDefined;
    }

    public BuilderContainer (int countRepeatedQr, String qr, String macAddress, TypeQR typeOfLastReadQr) {
        this.countOfContainersQrRepeated = countRepeatedQr;
        this.containerQr = qr;
        this.macAddress = macAddress;
        this.typeOfLastReadQr = typeOfLastReadQr;
    }

    @Override
    public String toString () {
        return super.toString();
    }
    // определение типа считанног штрихкода
    public void defineTypeOfReadQRCode(String readCode) {
        if ((readCode.length() == 18 && readCode.charAt(0) == '2') ||
                (readCode.length() == 20 && readCode.charAt(0) == '0' && readCode.charAt(2) == '2')) {
            typeOfLastReadQr = TypeQR.Container;
            containerQr = readCode;
        }
        else if (readCode.length() == 18 || readCode.length() == 20)
            typeOfLastReadQr = TypeQR.Box;
        else
            typeOfLastReadQr = TypeQR.NotDefined;
    }
    // анализ штрихкодов
    public void analyseReadQrCode (String readCode) {
        new LogService().saveLog(readCode, "Обработка штрихкода", LvlEvent.SYSTEM_INFO, macAddress);
        if (typeOfLastReadQr.equals(TypeQR.Container)) {
            buildingContainer(readCode);
        }
        else if (typeOfLastReadQr.equals(TypeQR.Box)) {
            readQrIsBoxBuildIt();
        }
        else {
            countOfContainersQrRepeated = countOfContainersQrRepeated == 3 ? 2 : countOfContainersQrRepeated;
            errorMsgContainerQrDontFind();
        }
    }
    // обработка считывания пользователем штрихкода короба
    public void buildingContainer(String readCode) {
        /*TransactionService transactionService = new TransactionService();
        transactionService.openTransaction(currentBuilder);
        new LogService().saveLog("", "Транзакция открыта", LvlEvent.CRITICAL, macAddress);*/
        switch (countOfContainersQrRepeated) {
            case 0:
                startOfContainerAssembly(readCode);
git             case 1:
                confirmationOfContainerAssembly(readCode);
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
    // старт сборки короба, подгрузка всех штрихкодов
    public String confirmationOfContainerAssembly(String bufferCode) {
        if (readCode.equals(historyBox.get(0))) {
            Container container = containerRepo.findByNumberContainer(readCode).get();
            String statusCont = container.getStatus();
            if (statusCont.equals("В сборке")) {
                List<ContainerContent> cont = contContentRepo.findByNumberContainer(readCode);
                if (cont.size() > 0) {
                    String macAddressAFewTimes = cont.get(0).getMacAddress();
                    if (macAddressAFewTimes.equals(macAddress)) {
                        backToSession();
                        messageToPeople("Сборка короба будет продолжена");
                        saveLog(readCode, "Сборка короба " + container.getNumberContainer() + " восстановлена", LvlEvent.INFO, macAddress);
                        isStarted = true;
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
            errorMsgContScanQRAnotherCont();
        }
    }

    public void initialAssemblyContainer (Container cont) {
        // надо засетапить грид во вью
        okMsgContBuildWasStarted();
        cont = containerRepo.findByNumberContainer(readCode).get();
        cont.setStatus("В сборке");
        containerRepo.save(cont);
        isStarted = true;
    }

    public void startCancelOfContainerAssembly() {

    }

    public void cancelOfContainerAssembly() {

    }

}

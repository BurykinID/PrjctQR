package com.qr.app.backend;

import com.qr.app.backend.entity.db.Transaction;
import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.entity.forSession.temporarytable.container.ContainerContent;
import com.qr.app.backend.entity.order.container.Container;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class BuilderContainer {

    private int countRepeatedQr;
    private String qr;

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
    public String confimationBuildContainer(BuilderContainer buildContainer, String bufferCode, String macAddress) {






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

}

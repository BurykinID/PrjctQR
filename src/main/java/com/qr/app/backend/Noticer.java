package com.qr.app.backend;

import com.qr.app.backend.entity.db.StateDB;
import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.service.LogService;
import com.qr.app.backend.service.StateDBService;
import org.springframework.beans.factory.annotation.Autowired;

import static com.qr.app.backend.Player.playSound;
import static com.qr.app.backend.service.LogService.saveLog;

public class Noticer {

    public static String readQrSomeSlowly() {

    }
    // сообщение о том, что БД заблокирована
    public static String okMsgDBIsLock(String bufferCode, String macAddress) {
        StateDB state = StateDBService.getDbState();
        String descriptionCurrentState = state.getDescription();
        saveLog(bufferCode, "Считан штрихкод. База заблокирована", LvlEvent.INFO, macAddress);
        playSound("Ошибка.wav");
        return descriptionCurrentState.isEmpty() ? "База заблокирована." : descriptionCurrentState;
    }
    // первый шаг прошел успешно. короб ещё не собран.
    public static String okMsgContStepOne(BuilderContainer builderContainer) {
        saveLog(builderContainer.getQr(), "Инициация сборки короба.", LvlEvent.INFO, builderContainer.getMacAddress());
        playSound("Подтвердите_начало_сборки_короба_сканированием_штрихкода_короба_еще_раз.wav");
        return "Отсканируйте штрихкод ещё раз";
    }
    // Сборка короба начата
    public static String okMsgContBuildWasStarted(BuilderContainer builderContainer) {
        saveLog(builderContainer.getQr(), "Сборка короба " + builderContainer.getQr() + " начата.", LvlEvent.INFO, builderContainer.getMacAddress());
        playSound("Сборка_короба_начата._Отсканируйте_товары_согласно_сборочного_листа.wav");
        return "Сборка короба начата";
    }
    // Сборка короба запущена на другом компьютере
    public static String errorMsgContAssembledInAnotherPC(BuilderContainer builderContainer) {
        saveLog(builderContainer.getQr(), "Сборка короба " + builderContainer.getQr() + " запущена на другом компьютере", LvlEvent.INFO, builderContainer.getMacAddress());
        playSound("Ошибка.wav");
        return "Сборка короба запущена на другом компьютере";
    }
    // Задание на сборку для короба не найдено в базе данных!
    public static String errorMsgContExerciseDontFind (BuilderContainer builderContainer) {
        saveLog(builderContainer.getQr(), "Задание на сборку для короба " + builderContainer.getQr() + " не найдено в базе данных!", LvlEvent.WARNING, builderContainer.getMacAddress());
        playSound("Ошибка.wav");
        return "Задание на сборку для короба " + builderContainer.getQr() + " не найдено в базе данных!";
    }
    // Короб уже собран
    public static String errorMsgContAssembled(BuilderContainer builderContainer) {
        saveLog(builderContainer.getQr(), "Сборка короба " + builderContainer.getQr() + " уже выполнена", LvlEvent.WARNING, builderContainer.getMacAddress());
        playSound("Короб_уже_собран.wav");
        return "Короб уже собран";
    }
    // Сборка не запущена. Считан штрихкод другого короба
    public static String errorMsgContScanQRAnotherCont(BuilderContainer builderContainer) {
        saveLog(builderContainer.getQr(),"Сборка не запущена. Считан  штрихкод другого короба:", LvlEvent.WARNING, builderContainer.getMacAddress());
        playSound("Ошибка.wav");
        return "Ошибка! Считан штрихкод другого короба: " + builderContainer.getQr();
    }
    // Короб не найден!
    public static String errorMsgContDontFind(BuilderContainer builderContainer) {
        saveLog(builderContainer.getQr(), "Короб " + builderContainer.getQr() + " не найден в базе данных", LvlEvent.WARNING, builderContainer.getMacAddress());
        playSound("Штрихкод_не_опознан.wav");
        return "Короб не найден!";
    }
    // штрихкод упаковки не распознан
    public static String errorMsgContQrDontFind(BuilderContainer builderContainer) {
        saveLog(builderContainer.getQr(), "Считанный штрихкод не опозан", LvlEvent.INFO, builderContainer.getMacAddress());
        playSound("Штрихкод_не_опознан.wav");
        return "Считан неопознанный штрихкод!" + builderContainer.getQr();
    }
    // Считан неопознанный штрихкод. контейнер
    public static String errorMsgContStepOne(BuilderContainer builderContainer) {
        saveLog(builderContainer.getQr(), "Считан неопознанный штрихкод ", LvlEvent.INFO, builderContainer.getMacAddress());
        playSound("Штрихкод_не_опознан.wav");
        return "Считан неопознанный штрихкод " + builderContainer.getQr();
    }

}

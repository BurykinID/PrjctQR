package com.qr.app.backend;

import com.qr.app.backend.entity.db.StateDB;
import com.qr.app.backend.entity.forSession.LvlEvent;
import com.qr.app.backend.service.LogService;
import com.qr.app.backend.service.StateDBService;

import java.util.List;

public class Noticer {

    public void readQrSomeSlowly() {
        //messageToPeople("Произошла ошибка. Считайте штрихкод ещё раз, чуть медленнее.");
        Player.playSound("Ошибка.wav");
    }
    // вывод сообщений пользователю о том, что он считал штрихкод не того короба
    public void errorScanBox(boolean isStarted, String bufferCode, String historyBox, String macAddress) {
        if (isStarted) {
            //messageToPeople("Ошибка! Штрихкод не распознан! " + bufferCode);
            LogService.saveLog(bufferCode, "Сборка короба " + historyBox + ". Считан неопознанный штрихкод", LvlEvent.WARNING, macAddress);
            Player.playSound("Штрихкод_не_опознан.wav");
        }
        else {
            //messageToPeople("Ошибка! Штрихкод не распознан! " + bufferCode);
            LogService.saveLog(bufferCode, "Сборка короба не запущена. Считан неопознанный штрихкод", LvlEvent.WARNING, macAddress);
            Player.playSound("Штрихкод_не_опознан.wav");
        }
    }
    // Для начала сборки необходимо отсканировать штрихкод короба!
    public static void errorMsgBoxAttentionBuildDontStart (String bufferCode, String macAddress) {
        //messageToPeople("Внимание!\r\nДля начала сборки необходимо отсканировать штрихкод короба!");
        LogService.saveLog(bufferCode, "Считан штрихкод марки. Сборка короба ещё не начата.", LvlEvent.WARNING, macAddress);
        Player.playSound("Перед_сканированием_марки_начните_сборку_короба.wav");
    }

    public static String okMsgDBIsLock(String bufferCode, String macAddress) {
        StateDB state = StateDBService.getDbState();
        String descriptionCurrentState = state.getDescription();
        LogService.saveLog(bufferCode, "Считан штрихкод. База заблокирована", LvlEvent.INFO, macAddress);
        Player.playSound("Ошибка.wav");
        return descriptionCurrentState.isEmpty() ? "База заблокирована." : descriptionCurrentState;
    }

}

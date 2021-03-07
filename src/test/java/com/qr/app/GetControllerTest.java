package com.qr.app;

import com.qr.app.backend.Json.get.HierarchyOfBox;
import com.qr.app.backend.Json.get.JsonReturn;
import com.qr.app.backend.Json.get.MarkAndContainerJson;
import com.qr.app.backend.Json.get.MarkJson;
import com.qr.app.backend.controllers.get.GetController;
import com.qr.app.backend.repository.HierarchyOfBoxesRepository;
import com.qr.app.backend.repository.MarkRepository;
import com.qr.app.backend.repository.db.StateDBRepository;
import com.qr.app.backend.repository.db.TransactionRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;

@RunWith (SpringRunner.class)
@SpringBootTest
public class GetControllerTest {

    @Autowired
    private MarkRepository markRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private StateDBRepository stateDBRepository;
    @Autowired
    private HierarchyOfBoxesRepository hierarchyOfBoxesRepository;

    @Test
    public void getStateDB() {

        GetController getController = new GetController(markRepository, transactionRepository, stateDBRepository, hierarchyOfBoxesRepository);
        Assert.assertNotNull(getController.getStateDB());

    }

    @Test
    public void getContainerAndMarkFrom() {
        GetController getController = new GetController(markRepository, transactionRepository, stateDBRepository, hierarchyOfBoxesRepository);
        MarkAndContainerJson json = getController.getSample("2019-01-07 10:00:00");
        List<HierarchyOfBox> containerJson = json.getContainerJson();
        List<MarkJson> markJsons = json.getMarkJson();
        Assert.assertNotNull(containerJson);
        Assert.assertNotNull(markJsons);
    }

    @Test
    public void getMark() {
        GetController getController = new GetController(markRepository, transactionRepository, stateDBRepository, hierarchyOfBoxesRepository);
        JsonReturn ret = getController.checkMarkWithPartQueryInUrl("010290000076453821__\">)FFA4L!xe", "1");
        Assert.assertEquals(ret.getResultRequest(), "Ошибка, марка уже добавлена в этот короб");
    }

}

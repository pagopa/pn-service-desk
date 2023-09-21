package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.NotifyDeliveryPushAction;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.model.NotifyEventDTO;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

@Component
@CustomLog
@AllArgsConstructor
public class NotifyDeliveryPushActionImpl implements NotifyDeliveryPushAction {

    private PnDeliveryPushClient pnDeliveryPushClient;

    @Override
    public void execute(NotifyEventDTO notifyEventDTO) {



    }
}

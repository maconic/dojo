package persistence.reposervice;

import io.reactivex.Observable;
import javaslang.collection.List;
import lombok.extern.log4j.Log4j2;
import oracle.jdbc.dcn.DatabaseChangeEvent;
import oracle.jdbc.dcn.QueryChangeDescription;
import oracle.jdbc.dcn.RowChangeDescription;
import oracle.jdbc.dcn.TableChangeDescription;
import oracle.sql.ROWID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import persistence.dao.ChatMsgEntity;

@Service
@Log4j2
public class ChatMsgNotificationService {

    private final ChatMsgDbChangeNotification chatMsgDbChangeNotification;

    private final ChatMsgRepoService chatMsgRepoService;

    @Autowired
    public ChatMsgNotificationService(ChatMsgDbChangeNotification chatMsgDbChangeNotification, ChatMsgRepoService chatMsgRepoService) {
        this.chatMsgDbChangeNotification = chatMsgDbChangeNotification;
        this.chatMsgRepoService = chatMsgRepoService;
    }

    public Observable<ChatMsgEntity> startListeningForNewEntities() {
        return chatMsgDbChangeNotification
                .startListeningForNotifications()
                .flatMap(this::getRowIdForChangeEvent)
                .map(ROWID::stringValue)
                .flatMap(chatMsgRepoService::findAllById)
                .doOnError(err -> log.error("startListeningForNewEntities error'd", err));
    }

    private Observable<ROWID> getRowIdForChangeEvent(DatabaseChangeEvent changeEvent) {
        return Observable.fromIterable(
                List.of(changeEvent.getQueryChangeDescription())
                        .map(QueryChangeDescription::getTableChangeDescription)
                        .flatMap(List::of)
                        .map(TableChangeDescription::getRowChangeDescription)
                        .flatMap(List::of)
                        .map(RowChangeDescription::getRowid));

    }

}



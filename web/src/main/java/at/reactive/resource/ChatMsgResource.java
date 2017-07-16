package at.reactive.resource;


import at.reactive.chat.ChatMsgDomainService;
import at.reactive.rto.ChatMsgRto;
import at.reactive.transformer.chat.ChatMsgRtoTransformer;
import org.glassfish.jersey.server.ChunkedOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Path("/chatMsg")
public class ChatMsgResource {

    private final ChatMsgDomainService chatMsgDomainService;

    @Autowired
    public ChatMsgResource(ChatMsgDomainService chatMsgDomainService) {
        this.chatMsgDomainService = chatMsgDomainService;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ChunkedOutput<ChatMsgRto> getChatMsgById(@PathParam("id") String chatMsgId) {
        final ChunkedOutput<ChatMsgRto> output = new ChunkedOutput<>(ChatMsgRto.class);
        chatMsgDomainService
                .chatMsgsById(chatMsgId)
                .map(ChatMsgRtoTransformer::toRto)
                .doFinally(output::close)
                .subscribe(output::write, err -> System.out.printf("BAM"));

        return output;
    }

    @GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public ChunkedOutput<List<ChatMsgRto>> getChatMsgByName(@QueryParam(value = "name") String chatMsgName) throws IOException {
        final ChunkedOutput<List<ChatMsgRto>> output = new ChunkedOutput<>(ChatMsgRto.class);
        List<ChatMsgRto> resultList = new ArrayList<>();
        chatMsgDomainService
                .chatMsgsByNameLike(chatMsgName)
                .map(ChatMsgRtoTransformer::toRto)
                .subscribe(resultList::add);

        output.write(resultList); //TODO: do it real? rx style
        output.close();
        return output;
    }


}
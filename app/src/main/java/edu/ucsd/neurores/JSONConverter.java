package edu.ucsd.neurores;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

/**
 * Created by tbpetersen on 9/14/2017.
 */

public class JSONConverter {

    public static List<Message> getMessageListJSON(String json,  HashMap<Long, User> users){
        Gson gson = createGson(true);
        Type listType = new TypeToken<List<Message>>() {}.getType();

        List<Message> messages =  gson.fromJson(json, listType);

        for(Message message : messages){
            if(message == null){
                continue;
            }

            User user = users.get(message.getSenderID());
            if(user == null){
                message.setSender("");
            }
            else{
                message.setSender(user.getName());
            }
        }

        return messages;
    }


    public static String userListToJSON(List<User> list){
        return createGson(true).toJson(list);
    }

    public static String messageListToJSON(List<Message> list){
        return createGson(true).toJson(list);
    }

    private static Gson createGson(final boolean serializeNulls){
        final GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(Message.class, new MessageSerializer());
        builder.registerTypeAdapter(User.class, new UserSerializer());
        builder.setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES);
        if (serializeNulls) {
            builder.serializeNulls();
        }
        return builder.create();
    }

    private static class MessageSerializer implements JsonSerializer<Message>, JsonDeserializer<Message> {

        @Override
        public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message_id", src.getMessageID());
            jsonObject.addProperty("text", src.getMessageText());
            jsonObject.addProperty("conv_id", src.getConversationID());
            jsonObject.addProperty("sender", src.getSenderID());
            jsonObject.addProperty("date", src.getTimeStringFormattedForDB());

            return jsonObject;
        }
        public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SimpleDateFormat formatter = Message.getFormatter();

            long user_id;
            long messageID;
            long conversationID;
            String text;
            Date date;

            try{
                JsonObject jsonObject = (JsonObject) json;
                user_id = jsonObject.get("sender").getAsLong();
                messageID = jsonObject.get("message_id").getAsLong();
                conversationID = jsonObject.get("conv_id").getAsLong();
                text = jsonObject.get("text").getAsString();
                String dateString = jsonObject.get("date").getAsString();
                date = formatter.parse(dateString);
                // TODO Work with timeszones
                date = new Date(date.getTime() - (1000 * 60 * 60 * 7));
            }catch(ParseException e){
                return null;
            }

            return new Message(messageID, conversationID, user_id, text, date.getTime());
        }
    }

    private static class UserSerializer implements JsonSerializer<User> {
        @Override
        public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            String superUser = null;
            jsonObject.addProperty("email", src.getName());
            jsonObject.addProperty("user_type", src.userType);
            jsonObject.addProperty("super_user", superUser);
            jsonObject.addProperty("isOnline", false);
            return jsonObject;
        }
    }
}

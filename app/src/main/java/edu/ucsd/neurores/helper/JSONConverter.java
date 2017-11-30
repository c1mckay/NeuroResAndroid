package edu.ucsd.neurores.helper;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.ucsd.neurores.abstraction.Conversation;
import edu.ucsd.neurores.activities.MainActivity;
import edu.ucsd.neurores.abstraction.Message;
import edu.ucsd.neurores.abstraction.User;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

/**
 * Created by tbpetersen on 9/14/2017.
 */

public class JSONConverter {

    public static List<Message> toMessageList(String json, HashMap<Long, User> users){
        Gson gson = createGson(true);
        Type listType = new TypeToken<List<Message>>() {}.getType();

        List<Message> messages =  gson.fromJson(json, listType);

        for(Message message : messages){
            if(message == null){
                Log.v("taggy", "Null message");
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

    public static List<User> toUserList(String json, Context context){
        Gson gson = createGson(true);
        Type listType = new TypeToken<List<User>>() {}.getType();

        Log.v("requestr", json);
        List<User> list = gson.fromJson(json, listType);
        for(User user : list){
            user.setContext(context);
        }
        return list;
    }



    public static <E> String toJSON(List<E> list){
        return createGson(true).toJson(list);
    }

    public static String conversationToJSON(List<Conversation> list, MainActivity mainActivity){
        JsonObject jsonObject = new JsonObject();
        for(Conversation conversation : list){
            if(conversation == null){
                continue;
            }
            JsonObject currentJSON = new JsonObject();
            String lastSeen = null;
            String conversationIDString = conversation.getID() + "";
            String unseenCount = null;

            if(conversation.hasUnreadMessages()){
                unseenCount = conversation.getNumOfUnread() + "";
            }

            JsonArray members = new JsonArray();
            List<Long> memberIDS = conversation.getUserIDs();

            for(Long memberID : memberIDS){
                members.add(memberID);
                members.add(mainActivity.loggedInUser.getID());
            }

            currentJSON.addProperty("last_seen", lastSeen);
            currentJSON.addProperty("unseen_count", unseenCount);
            currentJSON.add("members", members);

            jsonObject.add(conversationIDString, currentJSON);
        }
        return jsonObject.toString();
    }


    public static List<Conversation> toConversationList(String jsonString, HashMap<Long,User> userList){
        List<Conversation> conversationList = new ArrayList<Conversation>();

        try{
        /* The conversations in json form*/
            JSONObject conversationJSONObject = new JSONObject(jsonString);

        /* Calling "next" on the iterator will return the id of the next conversation (id in string form) */
            Iterator<String> iterator = conversationJSONObject.keys();
            Conversation currentConversation;
            String userID_s;
            long userID;
            // Iterate over all conversations
            while(iterator.hasNext()){
                String conversationId = iterator.next();
                currentConversation = new Conversation(Long.valueOf(conversationId), null);
                //currentConversation.setUnread(conversationJsonObject.lastUnread);
                JSONObject currentConversationObject = conversationJSONObject.getJSONObject(conversationId);
                String stringUnseen = currentConversationObject.getString("unseen_count");
                if(stringUnseen.equals("null")){
                    currentConversation.setNumOfUnread(0);
                }else{
                    currentConversation.setNumOfUnread(Integer.parseInt(stringUnseen));
                }

                JSONArray currentArray = currentConversationObject.getJSONArray("members");
                //JSONArray currentArray = conversationJSONObject.getJSONArray(conversationId).getJSONArray("members");
                // Iterate over users in conversations
                for(int i = 0; i < currentArray.length(); i++){
                    userID_s = currentArray.get(i).toString();
                    userID = Long.parseLong(userID_s);
                    if(userList.containsKey(userID)){
                        User u = userList.get(userID);
                        currentConversation.addUser(u);
                    }else{
                        Log.v("warning", "User with id " + userID + " in a conversation but not in the user list");
                    }
                }
                conversationList.add(currentConversation);
            }

        }catch( Exception e){
            Log.v("tag", "Failed to get JSONArray from json");
            e.printStackTrace();
        }

        return conversationList;
    }


    private static Gson createGson(final boolean serializeNulls){
        final GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(User.class, new UserSerializer());
        builder.registerTypeAdapter(Message.class, new MessageSerializer());

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
            long user_id;
            long messageID;
            long conversationID;
            String text;
            long milliseconds;

            JsonObject jsonObject = (JsonObject) json;
            user_id = jsonObject.get("sender").getAsLong();
            messageID = jsonObject.get("message_id").getAsLong();
            conversationID = jsonObject.get("conv_id").getAsLong();
            text = jsonObject.get("text").getAsString();
            String dateString = jsonObject.get("date").getAsString();
            milliseconds = Message.getMillisecondsFromTimeString(dateString);
            Message message = new Message(messageID, conversationID, user_id, text, milliseconds);
            return message;
        }
    }

    private static class UserSerializer implements JsonSerializer<User>, JsonDeserializer<User> {
        @Override
        public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            String superUser = null;
            jsonObject.addProperty("email", src.getName());
            jsonObject.addProperty("user_id", src.getID());
            jsonObject.addProperty("user_type", src.getUserType());
            jsonObject.addProperty("super_user", superUser);
            jsonObject.addProperty("isOnline", false);
            return jsonObject;
        }

        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = (JsonObject) json;
            String email = jsonObject.get("email").getAsString();
            String userType = jsonObject.get("user_type").getAsString();
            long userID = jsonObject.get("user_id").getAsLong();
            boolean isOnline = jsonObject.get("isOnline").getAsBoolean();

            return new User(null,userID,email,userType,isOnline);
        }
    }

}



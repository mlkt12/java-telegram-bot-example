import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        try {
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private List<String> meetingAcceptList = new ArrayList<>();
    private List<String> meetingDeclineList = new ArrayList<>();

    @Override
    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();

        if (message != null && message.hasText()) {

            String inputStream = message.getText();
            String userName = (message.getFrom().getUserName() == null || message.getFrom().getUserName().isEmpty()) ?
            message.getFrom().getFirstName() : message.getFrom().getUserName();

            switch (inputStream) {
                case CommandList.COMMAND_START:
                    sendMsg(message, helpMessage());
                    break;
                case CommandList.COMMAND_START_GROUP:
                    sendMsg(message, helpMessage());
                    break;

                case CommandList.COMMAND_MEETING:
                    onGetMeetingCommand(message, userName);
                    break;
                case CommandList.COMMAND_MEETING_GROUP:
                    onGetMeetingCommand(message, userName);
                    break;

                case CommandList.COMMAND_ACCEPT:
                    onGetAcceptCommand(message, userName);
                    break;
                case CommandList.COMMAND_ACCEPT_GROUP:
                    onGetAcceptCommand(message, userName);
                    break;

                case CommandList.COMMAND_DECLINE:
                    onGetDeclineCommand(message, userName);
                    break;
                case CommandList.COMMAND_DECLINE_GROUP:
                    onGetDeclineCommand(message, userName);
                    break;

                case CommandList.COMMAND_STAT:
                    onGetStatCommand(message);
                    break;
                case CommandList.COMMAND_STAT_GROUP:
                    onGetStatCommand(message);
                    break;

                case CommandList.COMMAND_HELP:
                    sendMsg(message, helpMessage());
                    break;
                case CommandList.COMMAND_HELP_GROUP:
                    sendMsg(message, helpMessage());
                    break;
            }

            if (message.getText().startsWith("/meetingTime")) {
                onGetMeetingTimeCommand(message, userName);
            }
        }

    }

    private void sendMsg(Message message, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());

        System.out.println(message.getFrom());
        System.out.println(message.getText());

        sendMessage.setText(s);
        try {
           sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void onGetMeetingCommand(Message message, String userName){
        meetingAcceptList.clear();
        meetingDeclineList.clear();
        sendMsg(message, userName+ ", invites to a meeting");
        meetingAcceptList.add(userName);
    }

    private void onGetMeetingTimeCommand(Message message, String userName) {
        if (message.getText().replaceAll("\\D","").isEmpty()) {
            sendMsg(message, userName+ ", you did not enter time");
        } else {
            meetingAcceptList.clear();
            meetingDeclineList.clear();
            meetingAcceptList.add(userName);
            sendMsg(message, userName+ ", invites to the meeting through "+message.getText().replaceAll("\\D","")+" minutes");
        }
    }

    private void onGetAcceptCommand(Message message, String userName){
            if (!meetingAcceptList.contains(userName)) {
                meetingAcceptList.add(userName);
                sendMsg(message, userName + ", goes to the meeting");
            } else {
                sendMsg(message, userName + ", you already agreed");
            }
            if (meetingDeclineList.contains(userName)) {
                meetingDeclineList.remove(userName);
            }
    }

    private void onGetDeclineCommand(Message message, String userName){
        if (!meetingAcceptList.contains(userName)) {
            sendMsg(message, userName + ", deny");
        } else {
            sendMsg(message, userName + ", changed his mind");
            meetingAcceptList.remove(userName);
        }
        if (!meetingDeclineList.contains(userName)) {
            meetingDeclineList.add(userName);
        }
    }

    private void onGetStatCommand(Message message) {
        if (meetingAcceptList.size() > 1) {
            String mesToSent = meetingAcceptList.get(0).toString()+" organized a meeting.\n"+"Will participate:\n";
            for (int i = 1; i < meetingAcceptList.size(); i++) {
                mesToSent += meetingAcceptList.get(i) + "\n";
            }
            if (meetingDeclineList.size() != 0) {
                mesToSent += "\nОтказались:\n";
                for (int i = 1; i < meetingDeclineList.size(); i++) {
                    mesToSent += meetingDeclineList.get(i) + "\n";
                }
            }
            sendMsg(message, mesToSent);
        } else if (meetingAcceptList.size() == 1) {
            sendMsg(message,meetingAcceptList.get(0).toString()+" organized a meeting, but so far no one has confirmed participation :(");
        } else {
            sendMsg(message,"No meeting - no participants :(");
        }
    }

    @Override
    public String getBotUsername() {
        return "MeetingBot";
    }

    @Override
    public String getBotToken() {
        return "PASS_YOUR_TOKEN_HERE";
    } 

    private String helpMessage(){
        return "Hello!\n" +
                "I'm a bot helping organize people’s meetings.\n" +
                "Here are my commands:\n"+
                "/meeting - invite people to a meeting;\n"+
                "/meetingTime %d (%d - any number) - invite to the meeting in %d minutes;\n"+
                "/mY - agree;\n"+
                "/mN - deny;\n"+
                "/meetingStat - see who goes to the meeting;\n"+
                "/meetingHelp - get a list of relevant commands.";
    }
}
package org.telegram.updateshandlers;

import java.util.ArrayList;
import java.util.List;

import org.telegram.BotConfig;
import org.telegram.database.DatabaseManager;
import org.telegram.database.DatabaseManager.WeatherSubscribe;
import org.telegram.services.CustomTimerTask;
import org.telegram.services.LanguageServices;
import org.telegram.services.TimerExecutor;
import org.telegram.services.WeatherServices;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.logging.BotLogger;

public class HKWeatherHandlers extends TelegramLongPollingBot {
    private static final String LOGTAG = "HKWEATHERHANDLERS";

    private static final int STARTSTATUS = 0;
    private static final int MAINMENU = 1;
    private static final int QUERYWEATHER = 2;
    private static final int SUBSCRIBE = 3;
    private static final int LANGUAGE = 4;
    
//    private static String userId;    
//    private static int state = STARTSTATUS;
//    private static String language = "en"; 
    
//    private static boolean isSubscribeCurrentWeather = false;
//    private static boolean isSubscribeWarning = false;
    
//    private static void setState(int mstate){
//    	state = mstate;
//    }
//
//    private static int getState(){
//    	return state;
//    }
    
//    private static void setLanguage(String mlanguage){
//    	language = mlanguage;
//    }
//
//    private static String getLanguage(){
//    	return language;
//    }
    
//    private static void setUserId(String muserId){
//    	userId = muserId;
//    }
//
//    private static String getUserId(){
//    	return userId;
//    }
    
//    private static void setIsSubscribeCurrentWeather(boolean misSubscribeCurrentWeather){
//    	isSubscribeCurrentWeather = misSubscribeCurrentWeather;
//    }
//
//    private static boolean getIsSubscribeCurrentWeather(){
//    	return isSubscribeCurrentWeather;
//    }
//    
//    private static void setIsSubscribeWarning(boolean misSubscribeWarning){
//    	isSubscribeWarning = misSubscribeWarning;
//    }
//
//    private static boolean getIsSubscribeWarning(){
//    	return isSubscribeWarning;
//    }
    
    public HKWeatherHandlers() {
        super();
        startAlertTimers();
    }

    @Override
    public String getBotToken() {
        return BotConfig.HKWEATHER_TOKEN;
    }
    
    private void startAlertTimers() {
    	for(int i=0; i<48; i++)
    	{    	
	        TimerExecutor.getInstance().startExecutionEveryDayAt(new CustomTimerTask(i + " alert", -1) {
	            @Override
	            public void execute() {
	                sendAlerts();
	            }
	        }, i%2, (i%2)*30, 0);
    	}
    }

    private void sendAlerts() {
    	List<WeatherSubscribe> allSubscribes = DatabaseManager.getInstance().getAllSubscribes();
        for (WeatherSubscribe weatherSubscribe : allSubscribes) {
            
	    	System.out.println(weatherSubscribe.getSubscribeCurrentWeather());
	    	System.out.println(weatherSubscribe.getSubscribeWarning());
	    	System.out.println(weatherSubscribe.getUserId());
    	
	        if (weatherSubscribe.getSubscribeCurrentWeather())
	        {
	            synchronized (Thread.currentThread()) {
	                try {
	                    Thread.currentThread().wait(100);
	                } catch (InterruptedException e) {
	                    BotLogger.severe(LOGTAG, e);
	                }
	            }
	        	
	            SendMessage sendMessage = new SendMessage();
	            sendMessage.enableMarkdown(true);
		        sendMessage.setChatId(String.valueOf(weatherSubscribe.getUserId()));
	        	
		        sendMessage.setText(WeatherServices.getInstance().fetchCurrentWeather(
		        		DatabaseManager.getInstance().getLanguage(weatherSubscribe.getUserId())));	
		        
		        try {
	                sendMessage(sendMessage);
	            } catch (TelegramApiException e) {
	                BotLogger.warn(LOGTAG, e);
	            } catch (Exception e) {
	                BotLogger.severe(LOGTAG, e);
	            }
	        }

            if (weatherSubscribe.getSubscribeWarning())
            {
                synchronized (Thread.currentThread()) {
                    try {
                        Thread.currentThread().wait(100);
                    } catch (InterruptedException e) {
                        BotLogger.severe(LOGTAG, e);
                    }
                }
            	
                SendMessage sendMessage = new SendMessage();
                sendMessage.enableMarkdown(true);
    	        sendMessage.setChatId(String.valueOf(weatherSubscribe.getUserId()));
            	
		        sendMessage.setText(WeatherServices.getInstance().fetchCurrentWarning(
		        		DatabaseManager.getInstance().getLanguage(weatherSubscribe.getUserId())));
		        
		        try {
	                sendMessage(sendMessage);
	            } catch (TelegramApiException e) {
	                BotLogger.warn(LOGTAG, e);
	            } catch (Exception e) {
	                BotLogger.severe(LOGTAG, e);
	            }
	        } 
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText() || message.hasLocation()) {
                    handleIncomingMessage(message);
                }
            }
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    @Override
    public String getBotUsername() {
        return BotConfig.HKWEATHER_USER;
    }    
 
    private void handleIncomingMessage(Message message) throws TelegramApiException {        

    	final String mlanguage = DatabaseManager.getInstance().getLanguage(message.getFrom().getId());    	
		
    	if(message.hasText())
    	{
    		if(message.getText().equals("/start"))
    		{
    			DatabaseManager.getInstance().setUserState(message.getFrom().getId(), MAINMENU);
    		}
    		else if(message.getText().equals("/stop")){
    			DatabaseManager.getInstance().setUserState(message.getFrom().getId(), STARTSTATUS);
    		}
    	}
    	
    	SendMessage sendMessageRequest;
        switch(DatabaseManager.getInstance().getUserState(message.getFrom().getId())) {
            case MAINMENU:
            	sendMessageRequest = messageOnMainMenu(message, mlanguage);
                break;
            case QUERYWEATHER:
            	sendMessageRequest = messageOnQueryWeather(message, mlanguage);
                break;
            case SUBSCRIBE:
            	sendMessageRequest = messageOnSubcribe(message, mlanguage);
                break;
            case LANGUAGE:
            	sendMessageRequest = messageOnLanguage(message, mlanguage);
                break;
            default:
                sendMessageRequest = sendMessageDefault(message, mlanguage);
                break;
        }
        sendMessage(sendMessageRequest);
    }
    
    private static SendMessage sendMessageDefault(Message message, String language) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(LanguageServices.getInstance().getString("sendMessageDefault", language));
        
        return sendMessage;
    }
    
    private static SendMessage messageOnMainMenu(Message message, String language) {   	
    	SendMessage sendMessageRequest;
        if (message.hasText()) {
            if (message.getText().equals(getAddGroupCommand(language))) {
                sendMessageRequest = onAddGroupChoosen(message, language);
            } else if (message.getText().equals(getQueryWeatherCommand(language))) {
                sendMessageRequest = onQueryWeatherChoosen(message, language);
            } else if (message.getText().equals(getSubscribeCommand(language))) {
                sendMessageRequest = onSubscribeChoosen(message, language);
            } else if (message.getText().equals(getLanguageCommand(language))) {
                sendMessageRequest = onLanguageChoosen(message, language);
            } else {
                sendMessageRequest = sendChooseOptionMessage(message.getChatId(), message.getMessageId(),
                    getMainMenuKeyboard(language), language);
            }
        } else {
            sendMessageRequest = sendChooseOptionMessage(message.getChatId(), message.getMessageId(),
                    getMainMenuKeyboard(language), language);
        }
        return sendMessageRequest;
    }
    
    private static SendMessage onAddGroupChoosen(Message message, String language) {
        SendMessage sendMessageRequest;
        
        sendMessageRequest = sendAddGroupChosenMessage(message.getFrom().getId(), message.getChatId(),
        		message.getMessageId());
        
        return sendMessageRequest;
    }
    
  
    private static SendMessage sendAddGroupChosenMessage(Integer userId, Long chatId, Integer messageId) {

        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest = new SendMessage();
        sendMessageRequest.enableMarkdown(true);
        sendMessageRequest.setChatId(chatId.toString());
        sendMessageRequest.setText("https://telegram.me/xxhkweatherbot?startgroup=true");
        sendMessageRequest.setReplyToMessageId(messageId);        
        sendMessageRequest.setReplyMarkup(getMainMenuKeyboard(DatabaseManager.getInstance().getLanguage(userId)));
        
		DatabaseManager.getInstance().setUserState(userId, MAINMENU);
        
        return sendMessageRequest;
    }
    
    private static SendMessage onQueryWeatherChoosen(Message message, String language) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        ReplyKeyboardMarkup replyKeyboardMarkup = getQueryWeathersKeyboard(language);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(getQueryWeathersMessage(language));
        
		DatabaseManager.getInstance().setUserState(message.getFrom().getId(), QUERYWEATHER);
        
        return sendMessage;
    }
    
    private static ReplyKeyboardMarkup getQueryWeathersKeyboard(String language) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboad(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(getCurrentWeatherCommand(language));
        keyboardFirstRow.add(getWarningCommand(language));
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }
    
    private static String getQueryWeathersMessage(String language) {
        return LanguageServices.getInstance().getString("onQueryWeatherCommand", language);
    }
    
    private static SendMessage messageOnQueryWeather(Message message, String language) {
        SendMessage sendMessageRequest = null;
        if (message.hasText()) {
            sendMessageRequest = sendQueryWeatherChosenMessage(message.getFrom().getId(), message.getChatId(),
                    message.getMessageId(), message.getText().trim());         
        }
        else{
        	sendMessageRequest = onQueryWeatherError(message.getChatId(), message.getMessageId(), language);
        }
        return sendMessageRequest;      
    }
    
    private static SendMessage onQueryWeatherError(Long chatId, Integer messageId, String language) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.enableMarkdown(true);
        sendMessageRequest.setChatId(chatId.toString());
        sendMessageRequest.setReplyMarkup(getLanguagesKeyboard(language));
        sendMessageRequest.setText(LanguageServices.getInstance().getString("errorQueryWeather", language));
        sendMessageRequest.setReplyToMessageId(messageId);

        return sendMessageRequest;
    }
    
    private static SendMessage sendQueryWeatherChosenMessage(Integer userId, Long chatId, Integer messageId, String queryweather) {
    	String responseToUser = LanguageServices.getInstance().getString("errorFetchingWeather", DatabaseManager.getInstance().getLanguage(userId));
    	if (queryweather.equals(getCurrentWeatherCommand(DatabaseManager.getInstance().getLanguage(userId)))){    		
    		responseToUser = WeatherServices.getInstance().fetchCurrentWeather(DatabaseManager.getInstance().getLanguage(userId)); 
    	} else if (queryweather.equals(getWarningCommand(DatabaseManager.getInstance().getLanguage(userId)))){    		
    		responseToUser = WeatherServices.getInstance().fetchCurrentWarning(DatabaseManager.getInstance().getLanguage(userId)); 
    	}
    	
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.enableMarkdown(true);
        sendMessageRequest.setChatId(chatId.toString());
        sendMessageRequest.setText(responseToUser);
        sendMessageRequest.setReplyToMessageId(messageId);
        sendMessageRequest.setReplyMarkup(getMainMenuKeyboard(DatabaseManager.getInstance().getLanguage(userId)));
        
		DatabaseManager.getInstance().setUserState(userId, MAINMENU);
        
        return sendMessageRequest;
    }
    
    private static SendMessage onSubscribeChoosen(Message message, String language) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        ReplyKeyboardMarkup replyKeyboardMarkup = getSubscribesKeyboard(language);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(getSubscribesMessage(language));
        
		DatabaseManager.getInstance().setUserState(message.getFrom().getId(), SUBSCRIBE);
        
        return sendMessage;
    }
    
    private static ReplyKeyboardMarkup getSubscribesKeyboard(String language) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboad(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(getSubscribeCurrentWeatherCommand(language));
        keyboardFirstRow.add(getSubscribeWarningCommand(language));
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(getUnsubscribeCurrentWeatherCommand(language));
        keyboardSecondRow.add(getUnsubscribeWarningCommand(language));
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }
    
    private static String getSubscribesMessage(String language) {
        return LanguageServices.getInstance().getString("onSubscribeCommand", language);
    }
    
    private static SendMessage messageOnSubcribe(Message message, String language) {
        SendMessage sendMessageRequest = null;
        if (message.hasText()) {
            sendMessageRequest = sendSubscribeChosenMessage(message.getFrom().getId(), message.getChatId(),
                    message.getMessageId(), message.getText().trim());         
        } 
        else{
        	sendMessageRequest = onSubscribeError(message.getChatId(), message.getMessageId(), language);
        }
        return sendMessageRequest;      
    }

    private static SendMessage onSubscribeError(Long chatId, Integer messageId, String language) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.enableMarkdown(true);
        sendMessageRequest.setChatId(chatId.toString());
        sendMessageRequest.setReplyMarkup(getLanguagesKeyboard(language));
        sendMessageRequest.setText(LanguageServices.getInstance().getString("errorSubscribeNotFound", language));
        sendMessageRequest.setReplyToMessageId(messageId);

        return sendMessageRequest;
    }
    
    private static SendMessage sendSubscribeChosenMessage(Integer userId, Long chatId, Integer messageId, String subscribe) {

    	if (subscribe.equals(getSubscribeCurrentWeatherCommand(DatabaseManager.getInstance().getLanguage(userId)))){    		
    		DatabaseManager.getInstance().setCurrentWeatherSubscribe(userId, true);
        	System.out.println("SUBSCRIBECURRENTWEATHER");
    	} else if (subscribe.equals(getSubscribeWarningCommand(DatabaseManager.getInstance().getLanguage(userId)))){    		
    		DatabaseManager.getInstance().setWarningSubscribe(userId, true);
        	System.out.println("SUBSCRIBEWARNING");
    	} else if (subscribe.equals(getUnsubscribeCurrentWeatherCommand(DatabaseManager.getInstance().getLanguage(userId)))){    		
    		DatabaseManager.getInstance().setCurrentWeatherSubscribe(userId, false);
        	System.out.println("UNSUBSCRIBECURRENTWEATHER");
    	} else if (subscribe.equals(getUnsubscribeWarningCommand(DatabaseManager.getInstance().getLanguage(userId)))){    		
    		DatabaseManager.getInstance().setWarningSubscribe(userId, false);
        	System.out.println("UNSUBSCRIBEWARNING");
    	}  	
    	
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.enableMarkdown(true);
        sendMessageRequest.setChatId(chatId.toString());
        sendMessageRequest.setText(LanguageServices.getInstance().getString("subscribeUpdated", DatabaseManager.getInstance().getLanguage(userId)));
        sendMessageRequest.setReplyToMessageId(messageId);
        sendMessageRequest.setReplyMarkup(getMainMenuKeyboard(DatabaseManager.getInstance().getLanguage(userId)));
        
		DatabaseManager.getInstance().setUserState(userId, MAINMENU);
        
        return sendMessageRequest;
    }
    
    private static SendMessage onLanguageChoosen(Message message, String language) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        ReplyKeyboardMarkup replyKeyboardMarkup = getLanguagesKeyboard(language);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(getLanguagesMessage(language));
        
		DatabaseManager.getInstance().setUserState(message.getFrom().getId(), LANGUAGE);
        
        return sendMessage;
    }
    
    private static String getLanguagesMessage(String language) {
        return LanguageServices.getInstance().getString("onLanguageCommand", language);
    }
    
    private static ReplyKeyboardMarkup getLanguagesKeyboard(String language) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboad(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(getEnglighLanguagesCommand(language));
        keyboardFirstRow.add(getChineseLanguagesCommand(language));
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }
    
    private static SendMessage messageOnLanguage(Message message, String language) {
        SendMessage sendMessageRequest = null;
        if (message.hasText()) {
        	if (LanguageServices.getInstance().getSupportedLanguages().values().contains(message.getText().trim())) {
                sendMessageRequest = sendLanguageChosenMessage(message.getFrom().getId(), message.getChatId(),
                        message.getMessageId(), message.getText().trim());
            } else {
                sendMessageRequest = onLanguageError(message.getChatId(), message.getMessageId(), language);
            }
        }
        return sendMessageRequest;
    }
    
    private static SendMessage onLanguageError(Long chatId, Integer messageId, String language) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.enableMarkdown(true);
        sendMessageRequest.setChatId(chatId.toString());
        sendMessageRequest.setReplyMarkup(getLanguagesKeyboard(language));
        sendMessageRequest.setText(LanguageServices.getInstance().getString("errorLanguageNotFound", language));
        sendMessageRequest.setReplyToMessageId(messageId);

        return sendMessageRequest;
    }

    private static SendMessage sendLanguageChosenMessage(Integer userId, Long chatId, Integer messageId, String language) {
        
    	String languageCode = LanguageServices.getInstance().getLanguageCodeByName(language);   
    	
    	DatabaseManager.getInstance().setLanguage(userId, languageCode);

        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.enableMarkdown(true);
        sendMessageRequest.setChatId(chatId.toString());
        sendMessageRequest.setText(LanguageServices.getInstance().getString("languageUpdated", DatabaseManager.getInstance().getLanguage(userId)));
        sendMessageRequest.setReplyToMessageId(messageId);
        sendMessageRequest.setReplyMarkup(getMainMenuKeyboard(languageCode));

		DatabaseManager.getInstance().setUserState(userId, MAINMENU);
        
        return sendMessageRequest;
    }
    
    private static ReplyKeyboardMarkup getMainMenuKeyboard(String language) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboad(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(getAddGroupCommand(language));
        keyboardFirstRow.add(getQueryWeatherCommand(language));
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(getSubscribeCommand(language));
        keyboardSecondRow.add(getLanguageCommand(language));
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }   
    
    private static SendMessage sendChooseOptionMessage(Long chatId, Integer messageId,
            ReplyKeyboard replyKeyboard, String language) {
    		SendMessage sendMessage = new SendMessage();
    		sendMessage.enableMarkdown(true);
    		sendMessage.setChatId(chatId.toString());
    		sendMessage.setReplyToMessageId(messageId);
    		sendMessage.setReplyMarkup(replyKeyboard);
    		sendMessage.setText(LanguageServices.getInstance().getString("chooseOption", language));
    		
    		return sendMessage;
	}
    
    private static String getCurrentWeatherCommand(String language) {
        return String.format(LanguageServices.getInstance().getString("currentweather", language), "");
    }
    
    private static String getWarningCommand(String language) {
        return String.format(LanguageServices.getInstance().getString("warning", language), "");
    }
    
    private static String getSubscribeCommand(String language) {
        return LanguageServices.getInstance().getString("subscribe", language);
    }
    
    private static String getLanguageCommand(String language) {
        return LanguageServices.getInstance().getString("language", language);
    }   
    
    private static String getEnglighLanguagesCommand(String language) {
        return LanguageServices.getInstance().getString("English", language);
    }
    
    private static String getChineseLanguagesCommand(String language) {
        return LanguageServices.getInstance().getString("Chinese", language);
    }
    
    private static String getSubscribeCurrentWeatherCommand(String language) {
        return LanguageServices.getInstance().getString("subscribecurrentweather", language);
    }
    
    private static String getSubscribeWarningCommand(String language) {
        return LanguageServices.getInstance().getString("subscribewarning", language);
    }
    
    private static String getUnsubscribeCurrentWeatherCommand(String language) {
        return LanguageServices.getInstance().getString("unsubscribecurrentweather", language);
    }
    
    private static String getUnsubscribeWarningCommand(String language) {
        return LanguageServices.getInstance().getString("unsubscribewarning", language);
    }
    
    private static String getQueryWeatherCommand(String language) {
        return LanguageServices.getInstance().getString("queryWeather", language);
    }
    
    private static String getAddGroupCommand(String language) {
        return LanguageServices.getInstance().getString("addGroup", language);
    }
  
}
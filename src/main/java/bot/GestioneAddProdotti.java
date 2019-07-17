package bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

class GestioneAddProdotti {

	static void aperturaCustumKeyboard(SendMessage message) {
		// Create ReplyKeyboardMarkup object
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		// Create the keyboard (list of keyboard rows)
		List<KeyboardRow> keyboard = new ArrayList<>();

		// Create a keyboard row
		KeyboardRow row = new KeyboardRow();
		row.add("Finito 🔚");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Pomodori 🍅");
		row.add("Insalata 🥗");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Olio 🛒");
		row.add("Pane 🍞");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Patate 🥔");
		row.add(("Merendine 🥐"));
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Biscotti 🍪");
		row.add("Birre 🍺");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Latte 🥛");
		row.add("Caffè ☕");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Pasta 🍝");
		row.add("Riso 🍚");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Tea ☕");
		row.add("Vino 🍷");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Formaggi 🧀");
		row.add("Salumi 🥓");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Scatolame 🥫");
		row.add("Yogurt 🍦");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Surgelati ❄");
		row.add("Verdure 🥦");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Snacks 🍿");
		row.add("Alcolici 🍾");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Agrumi 🍊");
		row.add("Acqua 💧");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Carne 🍖");
		row.add("Pesce 🦞");
		keyboard.add(row);

		row = new KeyboardRow();
		row.add("Detergenti 🧴");
		keyboard.add(row);

		// Set the keyboard to the markup
		keyboardMarkup.setKeyboard(keyboard);
		keyboardMarkup.setResizeKeyboard(true);

		message.setReplyMarkup(keyboardMarkup);

	}

	static SendMessage gestioneNuovoProd(Map<Long, List<Prodotto>> listeUtenti, Update update) {
		String answer = "";
		Long chat_id = update.getMessage().getChatId();
		String testoMex = update.getMessage().getText();
		String nomeP = testoMex.substring(0, testoMex.indexOf(" "));

		List<Prodotto> listaProdotti = listeUtenti.get(chat_id);
		System.out.println(nomeP);
		if (Utils.prodottoAmmissibile(nomeP)) {
			try {
				answer = GestioneAddProdotti.aggiungiProdotto(nomeP, chat_id, listaProdotti);
			} catch (NullPointerException e) {
				SendMessage message = new SendMessage().setChatId(chat_id).setText("Per favore premi /start");
				return message;
			}

		} else if (nomeP.equals("Finito")) {
			answer = "Lista aggiornata! Visualizzala con /lista";
			SendMessage new_message = new SendMessage().setChatId(chat_id).setText(answer);

			ReplyKeyboardRemove rem_custkeyboard = new ReplyKeyboardRemove();
			new_message.setReplyMarkup(rem_custkeyboard);

			return new_message;
		} else {
			return new SendMessage().setChatId(chat_id).setText("Comando non riconosciuto.");
		}

		SendMessage message = new SendMessage().setChatId(chat_id).setText(answer);

		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline = new ArrayList<>();
		rowInline.add(new InlineKeyboardButton().setText("Done ✅").setCallbackData("Done"));
		// Set the keyboard to the markup
		rowsInline.add(rowInline);
		// Add it to the message
		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);

		return message;
	}

	static String aggiungiProdotto(String nomeP, Long chat_id, List<Prodotto> listaProdotti) {
		Prodotto new_prod = new Prodotto(nomeP);
		if (listaProdotti.contains(new_prod))
			return "Prodotto già presente in lista, selezionane un altro o concludi con ''Done''";
		else {
			listaProdotti.add(new_prod);
			GestioneDB.aggiungiProdotto(chat_id, nomeP);
			return "Prodotto inserito in lista, selezionane un altro o concludi con ''Done''";
		}
	}
}

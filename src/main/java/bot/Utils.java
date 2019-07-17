package bot;

import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class Utils {

	private static final Map<String, String> tabellaEmoji = creaTabellaEmoji();

	private static Map<String, String> creaTabellaEmoji() {
		Map<String, String> myMap = new HashMap<String, String>();
		myMap.put("Pomodori", "🍅");
		myMap.put("Insalata", "🥗");
		myMap.put("Patate", "🥔");
		myMap.put("Merendine", "🥐");
		myMap.put("Birre", "🍺");
		myMap.put("Biscotti", "🍪");
		myMap.put("Caffè", "☕");
		myMap.put("Latte", "🥛");
		myMap.put("Tea", "☕");
		myMap.put("Scatolame", "🛒");
		myMap.put("Pasta", "🍝");
		myMap.put("Snacks", "🍿");
		myMap.put("Olio", "🛒");
		myMap.put("Pane", "🍞");
		myMap.put("Salse", "🍅");
		myMap.put("Riso", "🍚");
		myMap.put("Yogurt", "🍦");
		myMap.put("Formaggi", "🧀");
		myMap.put("Salumi", "🥓");
		myMap.put("Vino", "🍷");
		myMap.put("Surgelati", "❄");
		myMap.put("Verdure", "🥒");
		myMap.put("Agrumi", "🍊");
		myMap.put("Alcolici", "🍾");
		myMap.put("Carne", "🍖");
		myMap.put("Pesce", "🦀");
		myMap.put("Acqua", "💧");
		myMap.put("Detergenti", "🌀");
		
		return myMap;
	}

	static String getEmoji(String nome) {
		return tabellaEmoji.get(nome);
	}

	static boolean prodottoAmmissibile(String nome) {
		return tabellaEmoji.containsKey(nome);
	}
	
	static List<String> copiaLista(List<Prodotto> listaDaCopiare) { 
	    List<String> nuovaLista = new LinkedList<>();
	    for (Prodotto prodotto : listaDaCopiare) {
		nuovaLista.add(prodotto.getNome());
	    }
	    return nuovaLista;
	}

	static List<Offerta> leggiVolantinoXls(File fileXls) {
		List<Offerta> listaOfferteVolantino = new ArrayList<>();
		try {
			// Creiamo un Workbook dal file excel
			Workbook workbook = WorkbookFactory.create(fileXls);
			// recuperiamo la prima pagina del documento xls (che nel nostro caso ne
			// contiene una sola)
			Sheet sheet = workbook.getSheetAt(0);
			// Usiamo il DataFormatter per leggere i valori delle celle
			DataFormatter dataFormatter = new DataFormatter();

			boolean saltataIntestazione = false;
			// ora leggiamo le celle
			for (Row row : sheet) {
				if (saltataIntestazione) { // serve per evitare le intestazioni del file Xls
					int i = 0; // serve per capire quale campo si sta leggendo;
					Prodotto prod = null;
					int idSupermarket = 0;
					String descr = null;
					double prezzo = 0;

					for (Cell cell : row) { // leggiamo un campo per volta

						String cellValue = dataFormatter.formatCellValue(cell);
						if (!cellValue.equals("")) {
							if (i == 0)
								idSupermarket = Integer.parseInt(cellValue);
							else if (i == 1)
								prod = new Prodotto(cellValue);
							else if (i == 2)
								prezzo = Double.parseDouble(cellValue);
							else if (i == 3)
								descr = cellValue;
							i++;
						}else {
							return listaOfferteVolantino;
						}
					}
					Offerta offerta = new Offerta(prod, idSupermarket, prezzo, descr);
					listaOfferteVolantino.add(offerta);
				}
				saltataIntestazione = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listaOfferteVolantino;
	}

}

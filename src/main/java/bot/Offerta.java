package bot;

class Offerta {
	
	private Prodotto prodotto;
	private String descrizione;
	private double prezzo;
	private int idSupermercato;
	
	public Offerta(Prodotto prodotto, int idSupermercato, double prezzo, String descrizione) {
		this.prodotto = prodotto;
		this.descrizione = descrizione;
		this.prezzo = prezzo;
		this.idSupermercato = idSupermercato;
	}
	
	public Offerta(Prodotto prodotto, double prezzo, String descrizione) {
		this.prodotto = prodotto;
		this.descrizione = descrizione;
		this.prezzo = prezzo;
	}

	public Prodotto getProdotto() {
		return prodotto;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public double getPrezzo() {
		return prezzo;
	}

	public int getIdSupermercato() {
		return idSupermercato;
	}
	
	public String toString() {
		return idSupermercato+" "+prodotto.getNome()+" "+descrizione+" "+prezzo;
		
	}
	
	

}

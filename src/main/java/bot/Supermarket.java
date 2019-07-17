package bot;

class Supermarket {
	private long proprietario;
	private String nome, citta, indirizzo;
	private float longitudine, latitudine;
	private double distanzaUt = 0D;

	public Supermarket(long chat_id) {
		proprietario = chat_id;
	}

	public Supermarket(long chat_id, String nome, String citta, String ind,
			float latitudine, float longitudine) {
		this.proprietario = chat_id;
		this.nome = nome;
		this.citta = citta;
		this.indirizzo = ind;
		this.latitudine = latitudine;
		this.longitudine = longitudine;
	}
	
	public double getDistanzaUt() {
		return distanzaUt;
	}

	public void setDistanzaUt(double distanzaUt) {
		this.distanzaUt = distanzaUt;
	}

	public Supermarket(Supermarket s) {
		this.proprietario = s.proprietario;
		this.nome = s.nome;
		this.citta = s.citta;
		this.indirizzo = s.indirizzo;
		this.latitudine = s.latitudine;
		this.longitudine = s.longitudine;
	}

	public long getProprietario() {
		return proprietario;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getCitta() {
		return citta;
	}
	public void setCitta(String citta) {
		this.citta = citta;
	}
	public String getIndirizzo() {
		return indirizzo;
	}
	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}
	public float getLongitudine() {
		return longitudine;
	}
	public void setLongitudine(float longitudine) {
		this.longitudine = longitudine;
	}
	public float getLatitudine() {
		return latitudine;
	}
	public void setLatitudine(float latitudine) {
		this.latitudine = latitudine;
	}

	public String toString() {
		return proprietario + ", " + nome + ", " + citta + ", " + indirizzo
				+ ", " + latitudine + ", " + longitudine;
	}

}

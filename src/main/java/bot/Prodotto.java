package bot;

class Prodotto {
    
    private String nome;
    private String emoji; // in UNICODE

    public Prodotto(String nome) {
	this.nome = nome;
	this.emoji = Utils.getEmoji(nome);
    }

    public Prodotto(Prodotto p) {
	this.nome = p.nome;
	this.emoji = p.emoji;
    }

    public String getNome() {
	return nome;
    }

    public void setNome(String nome) {
	this.nome = nome;
    }

    public String getEmoji() {
	return emoji;
    }

    public void setEmoji(String emoji) {
	this.emoji = emoji;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == this)
	    return true;
	if (!(obj instanceof Prodotto))
	    return false;
	Prodotto p = (Prodotto) obj;
	if (p.nome.equals(nome))
	    return true;
	return false;
    }
    
    
}

package icai.dtc.isw.domain;

import java.io.Serializable;

public class User implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final String username;
    private final String password; // en demo; en real, usar hash
    private final String email;

    public User( String username, String password, String email) {
        this.id="";
        this.username = username;
        this.password = password;
        this.email = email;
    }
    public User(String id, String username, String password, String email) {
        this.id=id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getId() { return id; }

    @Override
    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clonación no soportada");
        }
    }

    public User deepClone() {
        return new User(this.id, this.username, this.password, this.email);
    }
}

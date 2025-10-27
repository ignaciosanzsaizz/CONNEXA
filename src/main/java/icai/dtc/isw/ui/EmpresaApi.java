package icai.dtc.isw.ui;

import icai.dtc.isw.client.Client;
import icai.dtc.isw.domain.Empresa;

import java.util.HashMap;

public class EmpresaApi {

    public Empresa getEmpresa(String mail) {
        Client c = new Client();
        HashMap<String,Object> session = new HashMap<>();
        session.put("mail", mail);
        session = c.sentMessage("/empresa/get", session);
        Object o = session.get("empresa");
        return (o instanceof Empresa) ? (Empresa) o : null;
    }

    public boolean saveEmpresa(String mail, String nombre, String nif, String sector) {
        Client c = new Client();
        HashMap<String,Object> session = new HashMap<>();
        session.put("mail", mail);
        session.put("empresaNombre", nombre);
        session.put("nif", nif);
        session.put("sector", sector);
        session = c.sentMessage("/empresa/save", session);
        Object ok = session.get("ok");
        return Boolean.TRUE.equals(ok);
    }
}

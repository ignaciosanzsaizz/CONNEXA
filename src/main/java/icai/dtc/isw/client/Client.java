package icai.dtc.isw.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;



import icai.dtc.isw.configuration.PropertiesISW;
import icai.dtc.isw.domain.Customer;
import icai.dtc.isw.message.Message;

public class Client {
	private String host;
	private int port;

	public Client(String host, int port) {
		this.host=host;
		this.port=port;
	}
	public Client() {
		this.host = PropertiesISW.getInstance().getProperty("host");
		this.port = Integer.parseInt(PropertiesISW.getInstance().getProperty("port"));
	}
	public HashMap<String, Object> sentMessage(String Context, HashMap<String, Object> session) {
		System.out.println("Host: "+host+" port"+port);

		Message mensajeEnvio=new Message();
		Message mensajeVuelta=new Message();
		mensajeEnvio.setContext(Context);
		mensajeEnvio.setSession(session);
		this.sent(mensajeEnvio,mensajeVuelta);


		switch (mensajeVuelta.getContext()) {
			case "/loginResponse":
				session=mensajeVuelta.getSession();

				break;

            case "/registerResponse":
                session = mensajeVuelta.getSession();
                break;

            case "/getCustomerResponse":
				session=mensajeVuelta.getSession();
				break;

			case "/empresaGetResponse":
				session = mensajeVuelta.getSession();
				break;

			case "/empresaSaveResponse":
				session = mensajeVuelta.getSession();
				break;

			case "/anuncioCreateResponse":
				session = mensajeVuelta.getSession();
				break;

			case "/anuncioGetResponse":
				session = mensajeVuelta.getSession();
				break;

			case "/anuncioListResponse":
				session = mensajeVuelta.getSession();
				break;

			case "/anuncioSearchResponse":
			case "/favoritosToggleResponse":
			case "/favoritosIsResponse":
			case "/favoritosListResponse":
			case "/chatListResponse":
			case "/chatGetOrCreateResponse":
		case "/chatMessagesResponse":
		case "/chatSendResponse":
		case "/chatReadResponse":
		case "/contratacionCrearResponse":
		case "/contratacionExisteResponse":
		case "/contratacionListResponse":
		case "/contratacionTerminarResponse":
		case "/contratacionValorarResponse":
		case "/contratacionEstadoResponse":
		case "/contratacionValoracionesResponse":
			session = mensajeVuelta.getSession();
			break;
			case "/pagoSaveResponse":
			case "/pagoGetResponse":
			case "/pagoDeleteResponse":
				session = mensajeVuelta.getSession();
				break;

			default:

				System.out.println("\nError a la vuelta");
				break;

		}
		//System.out.println("3.- En Main.- El valor devuelto es: "+((String)mensajeVuelta.getSession().get("Nombre")));
		return session;
	}



	public void sent(Message messageOut, Message messageIn) {
		try {

			System.out.println("Connecting to host " + host + " on port " + port + ".");

			Socket echoSocket = null;
			OutputStream out = null;
			InputStream in = null;

			try {
				echoSocket = new Socket(host, port);
				in = echoSocket.getInputStream();
				out = echoSocket.getOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);

				objectOutputStream.writeObject(messageOut);

				ObjectInputStream objectInputStream = new ObjectInputStream(in);
				Message msg=(Message)objectInputStream.readObject();
				messageIn.setContext(msg.getContext());
				messageIn.setSession(msg.getSession());

			} catch (UnknownHostException e) {
				System.err.println("Unknown host: " + host);
				System.exit(1);
			} catch (IOException e) {
				System.err.println("Unable to get streams from server");
				System.exit(1);
			}

			out.close();
			in.close();
			echoSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package com.ideas.controlador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;

import com.ideas.commons.ExceptionData;
import com.ideas.dao.DAOCategoria;
import com.ideas.dao.DAOJuego;
import com.ideas.dao.DAOJugador;
import com.ideas.entidades.Categoria;
import com.ideas.entidades.Juego;
import com.ideas.entidades.Jugador;
import com.ideas.entidades.Palabra;
import com.ideas.utilidades.UtilidadesCategoria;
import com.ideas.utilidades.UtilidadesPalabra;

public class ControlJuego extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DAOCategoria daoCategoria;
	private DAOJuego daoJuego;

	public ControlJuego() {
		super();
		// TODO Auto-generated constructor stub
		daoCategoria = new DAOCategoria();
		daoJuego = new DAOJuego();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// System.out.println("Action:" + request.getParameter("action"));
		String action = request.getParameter("action").toLowerCase();
		switch (action) {
		case "jugar":
		
			jugar(request, response);
			break;
		case "listarcategorias":
			listarCategorias(request, response);
			break;
		case "datospalabra":
			getPalabraCategoria(request, response);
			break;
		case "guardarjuego":
			guardarJuego(request, response);
			break;

		default:
			break;
		}

	}
		
	private void jugar(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {	
			HttpSession miSesion = request.getSession(true);
			Jugador jugador = ((Jugador) miSesion.getAttribute("usuario"));
			actualizarJuegosJugador(jugador, miSesion, "3");		
		} catch (ExceptionData e) {
			
			request.setAttribute("mensaje",e.getMessage());
		}
		
		mostrarForm(request, response, "jugar.jsp");
	}
	
	
	
	private void listarCategorias(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		JSONObject json = new JSONObject();

		try {
			List<Categoria> categorias = daoCategoria.listar("");

			json.put("estado", true);
			json.put("categorias", UtilidadesCategoria.jsonStringList(categorias));

		} catch (ExceptionData e) {

			json.put("estado", false);
			json.put("sms", e.getMessage());

		}

		response.getWriter().print(json.toJSONString());
	}

	private void getPalabraCategoria(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		JSONObject json = new JSONObject();

		try {

			String idCategoria = request.getParameter("idCategoria");
			Palabra palabra = daoJuego.getPalabraJuego(idCategoria);
			boolean estado = palabra != null;
			json.put("estado", estado);
			if (estado)
				json.put("palabra", UtilidadesPalabra.jsonString(palabra));
			else
				json.put("sms", "No existe palabras disponibles para la categoria");
		} catch (ExceptionData e) {

			json.put("estado", false);
			json.put("sms", e.getMessage());

		}
		response.getWriter().print(json.toJSONString());
	}

	private void guardarJuego(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		JSONObject json = new JSONObject();

		try {

			String fecha = request.getParameter("fechaJuego");
			String tiempo = request.getParameter("tiempo");
			String puntaje = request.getParameter("puntaje");
			String idPalabra = request.getParameter("idPalabra");
			String idJugador = "1";

			HttpSession miSession = request.getSession(true);
			if (miSession.getAttribute("usuario") != null) {
				Jugador jugador = ((Jugador) miSession.getAttribute("usuario"));
				Juego juego = new Juego();
				juego.setJugador(jugador);
				juego.setFecha(fecha);
				juego.setPuntaje(Double.parseDouble(puntaje));
				juego.setTiempo(Integer.parseInt(tiempo));
				juego.getPalabra().setIdPalabra(idPalabra);

				boolean estado = daoJuego.insert(juego);
				json.put("estado", true);
				json.put("sms", estado ? "Juego guardado con exito" : "No se guardo ningun registro de juego");

				// Actaulizar el atributo juegos del jugador
				if (estado)actualizarJuegosJugador(jugador, miSession, "3");
				
				System.out.println("Juego a insertar " + juego);
			}
		} catch (ExceptionData e) {

			json.put("estado", false);
			json.put("sms", e.getMessage());

		} catch (Exception e) {

			json.put("estado", false);
			json.put("sms", "Ocurrio un eror, no se guardo el juego");

		}
		response.getWriter().print(json.toJSONString());
	}

	public void actualizarJuegosJugador(Jugador jugador, HttpSession miSession, String limite) throws ExceptionData {
		List<Juego> juegos = new ArrayList<Juego>();
		juegos = daoJuego.listar(jugador, limite);
		jugador.setJuegos(juegos);		
		int numeroJuegos = daoJuego.numeroJuegos(jugador);
		miSession.setAttribute("usuario", jugador);
		miSession.setAttribute("numeroJuegos", numeroJuegos);
		
		// System.out.println("Llamada a este metodo");

	}
	
	private void mostrarForm(HttpServletRequest request, HttpServletResponse response, String path)
			throws ServletException, IOException {

		request.getRequestDispatcher(path).forward(request, response);

	}
}

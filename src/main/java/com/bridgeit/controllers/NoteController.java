package com.bridgeit.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bridgeit.entity.Note;
import com.bridgeit.entity.User;
import com.bridgeit.service.NoteService;
import com.bridgeit.service.UserService;

/**
 * @author Ajit Shikalgar Controller for managing notes such as CRUD operations,
 *         trashing, pinning etc.
 *
 */
@RestController("/usernotes")
public class NoteController {

	Logger logger = Logger.getLogger(NoteController.class);

	@Autowired
	NoteService noteService;

	@Autowired
	UserService userService;

	/**
	 * @param request
	 * @param response
	 * @return a function that returns list of notes for particular user.
	 */
	@GetMapping("/usernotes") // userId
	public ResponseEntity<List<Note>> showNotes(HttpServletRequest request, HttpServletResponse response) {
		Integer uId = (Integer) request.getAttribute("userId");
		logger.info("userId in request is: " + uId);
		if (uId == -1) {
			logger.info("User not found / Token validation failed");
			return new ResponseEntity<List<Note>>(HttpStatus.BAD_REQUEST);
		}
		List<Note> noteList = new ArrayList<>();

		noteList = noteService.getNoteList(uId);

		if (noteList == null) {
			System.out.println("No notes found for user ID: " + uId);
			return new ResponseEntity<List<Note>>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<List<Note>>(noteList, HttpStatus.OK);

	}

	/**
	 * @param note
	 * @param request
	 * @param response
	 * @return API that manages the creation of notes
	 */
	@PostMapping(value = "usernotes/createnote")
	public ResponseEntity<String> createNote(@RequestBody Note note, HttpServletRequest request,
			HttpServletResponse response) {
		Integer uId = (Integer) request.getAttribute("userId");
		logger.info("userId in request is: " + uId);

		logger.info("Id is: " + uId);
		try {
			noteService.createNote(uId, note);
			return new ResponseEntity<String>("Note Added.", HttpStatus.OK);
		} catch (Exception E) {
			E.printStackTrace();
			return new ResponseEntity<String>("User does not exist", HttpStatus.BAD_REQUEST);
		}

	}

	/**
	 * @param nId
	 * @param request
	 * @param response
	 * @return API to get a note of particular id
	 */
	@GetMapping(value = "usernotes/{nId}") // noteId
	public ResponseEntity<Note> showNote(@PathVariable("nId") Integer nId, HttpServletRequest request,
			HttpServletResponse response) {
		Integer uId = (Integer) request.getAttribute("userId");
		logger.info("userId in request is: " + uId);

		Note note;
		try {
			note = noteService.getNoteById(uId, nId);
		} catch (Exception E) {
			logger.error("Error Loading Note / Note not found");
			return new ResponseEntity<Note>(HttpStatus.NO_CONTENT);
		}

		System.out.println("Note is loaded");
		return new ResponseEntity<Note>(note, HttpStatus.OK);
	}

	/**
	 * @param updatedNote
	 * @param nId
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 *             API needed to manage updating of notes
	 */
	@PutMapping(value = "/usernotes/updatenote/{nId}")
	public ResponseEntity<Note> updateNote(@RequestBody Note updatedNote, @PathVariable("nId") Integer nId,
			HttpServletRequest request) throws IOException {
		Integer uId = (Integer) request.getAttribute("userId");
		logger.info("userId in request is: " + uId);
		Note oldNote = noteService.getNoteById(uId, nId);
		if (oldNote == null) {
			logger.info("User does not exist");
			return new ResponseEntity<Note>(HttpStatus.BAD_REQUEST);
		}
		updatedNote.getUser().setId(uId);
		updatedNote.setCreatedDate(oldNote.getCreatedDate());
		noteService.updateNote(updatedNote, nId);
		return new ResponseEntity<Note>(updatedNote, HttpStatus.OK);
	}

	/**
	 * @param nId
	 * @param request
	 * @param response
	 * @return in order to delete notes permanently, use deleteNode API
	 */
	@DeleteMapping(value = "usernotes/deletenote/{nId}")
	public ResponseEntity<String> deleteNode(@PathVariable("nId") Integer nId, HttpServletRequest request,
			HttpServletResponse response) {
		Integer uId = (Integer) request.getAttribute("userId");
		logger.info("userId in request is: " + uId);

		noteService.deleteNote(uId, nId);
		return new ResponseEntity<String>("Note Deleted. CHECK DB", HttpStatus.OK);
	}

	/**
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 *             API needed to get trashed notes list
	 */
	@GetMapping(value = "/usernotes/trashed")
	public ResponseEntity<List<Note>> getTrashedNoteList(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Integer uId = (Integer) request.getAttribute("userId");
		logger.info("userId in request is: " + uId);
		List<Note> trashedNoteList = new ArrayList<>();
		trashedNoteList = noteService.getTrashedNoteList(uId);
		PrintWriter out;
		if (trashedNoteList != null)
			return new ResponseEntity<List<Note>>(trashedNoteList, HttpStatus.OK);
		else {
			out = response.getWriter();
			out.print("Empty trash");
			return new ResponseEntity<List<Note>>(HttpStatus.NO_CONTENT);
		}
	}

	@PutMapping(value = "/usernotes/movetotrash/{nId}")
	public ResponseEntity<String> moveToTrash(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("nId") Integer nId) throws IOException {
		Integer uId = (Integer) request.getAttribute("userId");
		logger.info("userId in request is: " + uId);
		logger.info("noteId in request is: " + nId);
		User user = new User();
		user = userService.getUserById(uId, user);
		if (user == null) {
			logger.info("Note cannot be deleted. user not found");
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
		Note note = new Note();
		try {
		note = noteService.getNoteById(uId, nId);
		}
		catch(Exception E) {
		logger.info("Note Owner not found");	
		}
		System.out.println("Note is: "+note);
		try {
			if (uId.compareTo(note.getUser().getId()) == 0) {
				noteService.moveToTrash(nId);
				logger.info("Note moved to trash");
				return new ResponseEntity<String>(HttpStatus.OK);
			} else {
				Set<User> collabUsers = new HashSet<>();
				collabUsers = note.getCollabUsers();
				if (collabUsers == null) {
					logger.info("No collaboration for note Id: " + nId);
					return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
				}
				for (User tempUser : collabUsers) {
					if (tempUser.getId().compareTo(uId) == 0) {
						logger.info("Note will be Un-Collaborated. not deletion.");
						collabUsers.remove(user);
						return new ResponseEntity<String>(HttpStatus.OK);
					} else {
						return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
					}
				}
			}

		} catch (Exception E) {
			E.printStackTrace();
			return new ResponseEntity<String>("Note does not exists", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<String>("Note moved to trash", HttpStatus.OK);
	}

	@PostMapping(value = "/usernotes/collaborate")
	public ResponseEntity<String> collaborateNote(@RequestBody Note cNote, HttpServletRequest request,
			HttpServletResponse response) {
		logger.info("Note Object: " + cNote);
		if (cNote == null) {
			logger.info("Note Empty");
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
		Integer userId = (Integer) request.getAttribute("userId");
		logger.info("cUserId: " + request.getHeader("cUserId"));
		if (request.getHeader("cUserId") == null) {
			logger.info("*********No cUserId received*********");
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}

		logger.info("collaborating note with id: " + cNote.getNoteId());
		cNote = noteService.getNoteById(userId, cNote.getNoteId());
		if (cNote == null) {
			logger.info("Note does not exist");
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}
		Integer cUserId = Integer.parseInt(request.getHeader("cUserId"));
		// check if logged in user is editing note.
		// or collaborated users editing note //
		if (userId.compareTo(cNote.getUser().getId()) == 0) {
			User cUser = new User();
			cUser = userService.getUserById(cUserId, cUser);
			noteService.collaborateUser(cUser, cNote);

		} else {
			logger.info("Note Owner Authorization failed");
			return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<String>(HttpStatus.OK);
	}

}

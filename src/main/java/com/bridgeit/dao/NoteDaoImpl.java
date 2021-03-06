package com.bridgeit.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.log4j.Logger;
import org.dom4j.IllegalAddException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bridgeit.entity.Label;
import com.bridgeit.entity.Note;
import com.bridgeit.entity.User;

@Repository("noteDao")
public class NoteDaoImpl implements NoteDao {
	Logger logger = Logger.getLogger(NoteDaoImpl.class);
	@Autowired
	SessionFactory sessionFactory;

	@Override
	public Note getNoteById(Integer uId, Integer nId) {
		logger.info("got id in DAO as: " + uId);
		Note note = new Note();
		// get note from database
		Session session = sessionFactory.openSession();
		logger.info("Getting Note by Id: " + nId);
		note = (Note) session.get(Note.class, nId);
		// to verify note belongs to same user
		if (note.getUser().getId().compareTo(uId) == 0)
			return note;
		return null;
	}

	@Override
	public Note getCompleteNoteById(Integer nId) {
		logger.info("got id in DAO as: " + nId);
		Note note = new Note();
		// get note from database
		Session session = sessionFactory.openSession();
		logger.info("Getting Note by Id: " + nId);
		note = (Note) session.get(Note.class, nId);
		// to verify note belongs to same user
		if (note != null)
			return note;
		return null;
	}

	@Override
	public void updateNote(Note updatedNote, Integer nId) {
		logger.info("user id in updated note is : " + updatedNote.getUser().getId());
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		updatedNote.setNoteId(nId);
		updatedNote.setModifiedDate(LocalDateTime.now());
		session.update(updatedNote);
		tx.commit();
		session.close();
	}

	@Override
	public void moveToTrash(Integer nId) {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		Note note = session.get(Note.class, nId);
		note.setModifiedDate(LocalDateTime.now());
		note.setInTrash(true);
		tx.commit();
		return;
	}

	@Override
	public void restoreFromTrash(Integer nId) {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		Note note = session.get(Note.class, nId);
		note.setModifiedDate(LocalDateTime.now());
		note.setInTrash(false);
		tx.commit();
		return;
	}

	@Override
	public void pinNote(Note note) {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		note = session.get(Note.class, note.getNoteId());
		note.setPinned(true);
		tx.commit();
		return;
	}

	@Override
	public void archiveNote(Note note) {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		note = session.get(Note.class, note.getNoteId());
		note.setArchived(true);
		tx.commit();
		return;
	}

	@Override
	public void deleteNote(Integer uId, Integer nId) {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		logger.info("uId is: " + uId + " and nId is: " + nId);
		Note note = session.get(Note.class, nId);
		logger.info("note is: " + note);
		session.delete(note);
		tx.commit();
		session.close();
		return;
	}

	@Override
	public List<Note> getNoteList(Integer uId, String noteCategory) {
		// bring entire note list from database
		logger.info("Into getNoteList()");
		Session session = sessionFactory.openSession();
		// since session.createCriteria() is deprecated
		// Create CriteriaBuilder
		CriteriaBuilder builder = session.getCriteriaBuilder();

		// Create CriteriaQuery
		CriteriaQuery<Note> criteria = builder.createQuery(Note.class);

		criteria.from(Note.class);
		List<Note> entireNoteList = session.createQuery(criteria).getResultList();
		// learn a more efficient way to retrieve notes
		List<Note> noteList = new ArrayList<>();

		// retrieve notes of which user is owner
		if (entireNoteList.size() != 0)
			switch (noteCategory) {

			case "trashed":
				for (Note tempNote : entireNoteList)
					if (tempNote.getUser().getId().compareTo(uId) == 0 && tempNote.isInTrash() == true) {
						noteList.add(tempNote);
					} else {
						if ((tempNote.getCollabUsers() != null))
							for (User tempUser : tempNote.getCollabUsers()) {
								if (tempUser.getId().compareTo(uId) == 0) {
									noteList.add(tempNote);
								}
							}
					}
				// and the ones collaborated with user
				return noteList;

			case "archived":
				for (Note tempNote : entireNoteList)
					if (tempNote.getUser().getId().compareTo(uId) == 0 && tempNote.isArchived() == true) {
						noteList.add(tempNote);
					}
				// and the ones collaborated with user
				return noteList;

			case "pinned":
				for (Note tempNote : entireNoteList)
					if (tempNote.getUser().getId().compareTo(uId) == 0 && tempNote.isPinned() == true) {
						noteList.add(tempNote);
					}
				// and the ones collaborated with user
				return noteList;
			default:
				for (Note tempNote : entireNoteList)
					if (tempNote.getUser().getId().compareTo(uId) == 0 && tempNote.isInTrash() == false
							&& tempNote.isPinned() == false && tempNote.isArchived() == false) {
						noteList.add(tempNote);
					} else {
						if ((tempNote.getCollabUsers() != null))
							for (User tempUser : tempNote.getCollabUsers()) {
								if (tempUser.getId().compareTo(uId) == 0) {
									noteList.add(tempNote);
								}
							}
					}
				// and the ones collaborated with user
				return noteList;
			}
		return null;
	}

	@Override
	public List<Note> getAllNoteList(Integer uId) {
		// bring entire note list from database
		logger.info("Into getNoteList()");
		Session session = sessionFactory.openSession();
		// since session.createCriteria() is deprecated
		// Create CriteriaBuilder
		CriteriaBuilder builder = session.getCriteriaBuilder();

		// Create CriteriaQuery
		CriteriaQuery<Note> criteria = builder.createQuery(Note.class);

		criteria.from(Note.class);
		List<Note> entireNoteList = session.createQuery(criteria).getResultList();
		// learn a more efficient way to retrieve notes
		List<Note> noteList = new ArrayList<>();

		// retrieve notes of which user is owner
		if (entireNoteList.size() != 0)

			for (Note tempNote : entireNoteList)
				if (tempNote.getUser().getId().compareTo(uId) == 0) {
					noteList.add(tempNote);
				} else {
					if ((tempNote.getCollabUsers() != null))
						for (User tempUser : tempNote.getCollabUsers()) {
							if (tempUser.getId().compareTo(uId) == 0) {
								noteList.add(tempNote);
							}
						}
				}
		// and the ones collaborated with user
		return noteList;

	}

	@Override
	public List<Note> getTrashedNoteList(Integer uId) {
		Session session = sessionFactory.openSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Note> criteria = builder.createQuery(Note.class);
		criteria.from(Note.class);
		List<Note> entireNoteList = session.createQuery(criteria).getResultList();
		List<Note> noteList = new ArrayList<>();
		for (Note tempNote : entireNoteList)
			if (tempNote.getUser().getId().compareTo(uId) == 0 && tempNote.isInTrash()) {
				System.out.println("Tempnote: " + tempNote);
				noteList.add(tempNote);
			}
		return noteList;
	}

	@Override
	public List<Note> getCompleteTrashedNoteList() {
		Session session = sessionFactory.openSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Note> criteria = builder.createQuery(Note.class);
		criteria.from(Note.class);
		List<Note> entireNoteList = session.createQuery(criteria).getResultList();
		List<Note> noteList = new ArrayList<>();
		for (Note tempNote : entireNoteList)
			if (tempNote.isInTrash()) {
				System.out.println("Tempnote: " + tempNote);
				noteList.add(tempNote);
			}
		return noteList;
	}

	@Override
	public Note createNote(Integer uId, Note note) {
		logger.info("saving notes with user id :" + uId);
		Session session = sessionFactory.getCurrentSession();
		User user = new User();
		try {

			user = session.get(User.class, uId);
			note.setUser(user);
			note.setCreatedDate(LocalDateTime.now());
			note.setNoteId(null);
			session.save(note);
		} catch (Exception E) {
			E.printStackTrace();
			logger.info("User Id " + uId + " does not exist");
			return null;
		}
		logger.info("User is: " + user);

		return note;
	}

	@Override
	public void collaborateUser(User cUser, Note cNote) {
		Session session = sessionFactory.getCurrentSession();
		// Transaction tx = session.beginTransaction();
		boolean isNoteCollab = false;
		Set<User> collabUsers = cNote.getCollabUsers();
		if (collabUsers == null) {
			collabUsers = new HashSet<>();
		} else {
			// check if other user already collaborated
			for (User tempUser : collabUsers) {
				if (cUser.getId().compareTo(tempUser.getId()) == 0) {
					isNoteCollab = true;
					throw new IllegalAddException("User is already collaborated");
				}
			}
		}
		// add other to note collaboration
		if (!isNoteCollab) {
			cNote.getCollabUsers().add(cUser);
		}
		for (User tempUser : collabUsers) {
			System.out.println("***********" + tempUser.getFirstName());
		}
		session.merge(cNote);
		// tx.commit();
		/* session.close(); */
		return;
	}

	@Override
	public void unCollaborate(Note cNote, User cUser) {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		Set<User> collabUsers = new HashSet<>();
		collabUsers = cNote.getCollabUsers();
		if (collabUsers == null)
			return;
		boolean collaborated = false;
		for (User tempUser : collabUsers)
			if (tempUser.getId().compareTo(cUser.getId()) == 0) {
				collaborated = true;
				logger.info("Note will be Un-Collaborated. not deletion.");
				collabUsers.remove(cUser);
				session.merge(cNote);
				tx.commit();
				session.close();
				return;
			}
		if (!collaborated) {
			logger.info("User is not even collaborated. deletion failed");
			return;
		}
	}

	public void removeFromTrash(Note note) {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		session.delete(note);
		tx.commit();
		session.close();
	}

	@Override
	public Label createLabel(User user, Label label) {
		System.out.println("LABEL IS: " + label);
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		label.setId(null);
		label.setUser(user);
		session.save(label);
		tx.commit();
		session.close();
		return label;
	}

	@Override
	public List<Label> getLabels(User user) {

		logger.info("Into getLabels()");
		System.out.println("User is: " + user);
		Session session = sessionFactory.openSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Label> criteria = builder.createQuery(Label.class);
		criteria.from(Label.class);
		List<Label> entireLabelList = session.createQuery(criteria).getResultList();
		// learn a more efficient way to retrieve notes
		List<Label> labelList = new ArrayList<>();

		// retrieve labels of which user is owner
		if (entireLabelList.size() != 0)

			for (Label tempLabel : entireLabelList)
				if (tempLabel.getUser().getId().compareTo(user.getId()) == 0) {
					labelList.add(tempLabel);
				}
		return labelList;
	}

}

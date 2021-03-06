package com.bridgeit.entity;

/**
 * @author Ajit Shikalgar Entity used for login purposes since JSON can be
 *         parsed into object only
 */
public class UserLoginPair {

	private String email;
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "UserLoginPair [email=" + email + ", password=" + password + "]";
	}

	public UserLoginPair(String email, String password) {
		super();
		this.email = email;
		this.password = password;
	}

	public UserLoginPair() {

	}

}

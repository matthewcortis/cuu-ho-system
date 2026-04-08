package com.backend.cuutro.dto.response;

import java.io.Serializable;
import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResponseData<T> implements Serializable {

	int status;
	T data;
	String error;
	String message;
	@Builder.Default
	Date timestamp = new Date();
	String path;

	public ResponseData(int status, T data, String error, String message) {
		this.status = status;
		this.data = data;
		this.error = error;
		this.message = message;
		this.timestamp = new Date();
	}

	public ResponseData(int status, T data, String error, String message, String path) {
		this.status = status;
		this.data = data;
		this.error = error;
		this.message = message;
		this.path = path;
		this.timestamp = new Date();
	}
}

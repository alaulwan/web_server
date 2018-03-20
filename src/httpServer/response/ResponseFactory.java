package httpServer.response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
//import javax.xml.bind.DatatypeConverter;
import java.util.Base64;

import httpServer.Request;
import httpServer.Request.RequestType;
import httpServer.exceptions.CreatedException;
import httpServer.exceptions.ForbiddenException;
import httpServer.exceptions.FoundException;
import httpServer.exceptions.NotFoundException;

public class ResponseFactory {
	private File file;
	private Request recievedRequest;
	private Response response;

	// The next two arrays to serve the response 302 Found
	String[] oldLocationsArray = { "/pictures/img1.png", "/pictures/img2.png" };
	String[] newLocationsArray = { "/images/img1.png", "/images/img2.png" };

	public ResponseFactory(Request recievedRequest) {
		this.recievedRequest = recievedRequest;

		// Correct the file-separator char in the oldLocationsArray. The result depends
		// on the operation system
		for (int i = 0; i < oldLocationsArray.length; i++) {
			oldLocationsArray[i] = oldLocationsArray[i].replace('/', File.separatorChar);
		}
	}

	// Form the response according to the received request from the client
	public Response getResponse() {
		if (!recievedRequest.isHTTP()) {
			response = new ResponseHTTPVersionNotSupported505();
		} else if (recievedRequest.getRequestType() == RequestType.GET) {
			try {
				this.file = Get();
				response = new ResponseOk200(file);
			} catch (NotFoundException e) {
				response = new ResponseNotFound404(e.wrongPath);
			} catch (ForbiddenException e) {
				response = new ResponseForbidden403();
			} catch (FoundException e) {
				response = new ResponseFound302(e.correctPath);
			}
		} else if ((recievedRequest.getRequestType() == RequestType.POST)) {

			try {
				Post();
				response = new ResponseOk200(file);
			} catch (NotFoundException e) {
				response = new ResponseNotFound404(e.wrongPath);
			} catch (CreatedException e) {
				response = new ResponseCreated201(e.location);
			} catch (IOException e) {
				response = null;
			}

		} else if ((recievedRequest.getRequestType() == RequestType.PUT)) {

			try {
				Put();
				response = new ResponseOk200(file);
			} catch (CreatedException e) {
				response = new ResponseCreated201(e.location);
			} catch (IOException e) {
				response = null;
			}

		}

		return response;
	}

	private void Put() throws IOException, CreatedException {
		boolean isCreateNewFileRequest = false;
		
		//byte[] filecontentBytes = DatatypeConverter.parseBase64Binary(recievedRequest.getRecievedFilecontent());
		byte[] filecontentBytes = Base64.getDecoder().decode(recievedRequest.getRecievedFilecontent());
		
		File recievedFile = new File(recievedRequest.getOrginalRequestPath());

		// if the file is exists, then delete it. else, the request is to create a new
		// image
		if (recievedFile.exists())
			recievedFile.delete();
		else
			isCreateNewFileRequest = true;

		// The path of the new file which will be created
		String newFilePath = recievedFile.getPath().substring(0,
				recievedFile.getPath().length() - recievedFile.getName().length())
				+ recievedRequest.getRecievedFileName();

		File newFile = new File(newFilePath);
		OutputStream outputStream = new FileOutputStream(newFile, false);
		outputStream.write(filecontentBytes);
		outputStream.flush();
		outputStream.close();

		if (isCreateNewFileRequest)
			throw new CreatedException(newFilePath.replace(File.separatorChar, '/')
					.substring(recievedRequest.getParentFolder().getPath().length()));

	}

	private void Post() throws NotFoundException, IOException, CreatedException {
		boolean isCreateNewFileRequest = false;
		
		//byte[] filecontentBytes = DatatypeConverter.parseBase64Binary(recievedRequest.getRecievedFilecontent());
		byte[] filecontentBytes = Base64.getDecoder().decode(recievedRequest.getRecievedFilecontent());
		
		String filePath = recievedRequest.getOrginalRequestPath();
		File recievedFile = new File(filePath);

		if (!recievedFile.exists())
			throw new NotFoundException(recievedRequest.getOrginalRequestPath());

		/*
		 * If the path is a directory, then the POST request is to create a new image
		 * Therefore, the server should decide the folder to create the image file
		 */
		if (recievedFile.isDirectory()) {
			isCreateNewFileRequest = true;
			filePath = recievedRequest.getParentFolder().getPath() + File.separator + "images" + File.separator
					+ recievedRequest.getRecievedFileName();
			recievedFile = new File(filePath);
		}

		OutputStream outputStream = new FileOutputStream(recievedFile, false);
		outputStream.write(filecontentBytes);
		outputStream.flush();
		outputStream.close();

		if (isCreateNewFileRequest)
			throw new CreatedException(filePath.replace(File.separatorChar, '/')
					.substring(recievedRequest.getParentFolder().getPath().length()));
	}

	public File Get() throws NotFoundException, ForbiddenException, FoundException {
		File file = new File(recievedRequest.getPath());

		// check if the access to the path is restricted
		if (file.getPath().startsWith(recievedRequest.getParentFolder().getPath() + File.separator + "system")) {
			throw new ForbiddenException();
		}

		// If the path is a directory, then search for a .htm or .html file and return
		// it
		if (file.isDirectory()) {
			for (int i = 0; i < file.listFiles().length; i++) {
				if (file.listFiles()[i].getName().equals("index.htm")
						|| file.listFiles()[i].getName().equals("index.html")) {
					file = file.listFiles()[i];
					return file;
				}
			}
		}

		// if the path is existed file, then return it
		if (file.isFile()) {
			if (file.exists()) {
				return file;
			}
		}

		// if the server could not find the path, then check if the path was changed
		if (!file.isFile() && !file.isDirectory()) {
			for (int i = 0; i < oldLocationsArray.length; i++) {
				if (file.getPath().equals(recievedRequest.getParentFolder().getPath() + oldLocationsArray[i])) {
					String correctPath = newLocationsArray[i];
					throw new FoundException(correctPath);
				}
			}

		}

		// If reach this line without finding the path, then the path is not exists
		throw new NotFoundException(recievedRequest.getPath());
	}

}

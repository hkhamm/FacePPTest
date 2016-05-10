import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import java.io.File;
import java.net.URI;
import java.util.Objects;

/**
 * Facilitates communication between the authentication server and the FacePlusPlus API.
 * Makes HTTP requests to the API to create groups of people. Each person has a set of face
 * images. These are used to determine if a provided image of a person's face matches any
 * of the people in the group.
 */
public class FacePPAPICommunicator {

    private String apiKey;
    private String apiSecret;
    private Boolean useChineseServer;
    private Boolean useHttp;
    private HttpRequests httpRequests;
    private String groupName;

    /**
     * Constructor
     */
    public FacePPAPICommunicator(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        useChineseServer = false;
        useHttp = true;
        groupName = "group_0";

        initializeHttpRequests();
    }

    /**
     * Initializes the HTTP request object with the API key and API secret.
     */
    private void initializeHttpRequests() {
        httpRequests = new HttpRequests(apiKey, apiSecret, useChineseServer, useHttp);
    }

    /**
     * Attempts to detect a face in the image at the given URL.
     * @param url the image URL
     * @return the FacePP FaceId string
     */
    String detectFace(String url) {
        System.out.println("\n[FPP] Detecting face from URL");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.detectionDetect(new PostParameters().setUrl(url));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return getFaceId(response);
    }

    /**
     * Attempts to detect a face in the image file.
     * @param file the image file
     * @return the result string
     */
    String detectFace(File file) {
        System.out.println("\n[FPP] Detecting face from file");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.detectionDetect(new PostParameters().setImg(file));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response.toString();
    }

    /**
     * Attempts to detect a face in the image byte array.
     * @param data the image byte array
     * @return the result string
     */
    String detectFace(byte[] data) {
        System.out.println("\n[FPP] Detecting face from byte[]");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.detectionDetect(new PostParameters().setImg(data));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response.toString();
    }

    /**
     * Creates a person with the given name.
     * @param personName the person's name
     * @return the response string
     */
    String createPerson(String personName) {
        System.out.println("\n[FPP] Creating person");
        JSONObject response = new JSONObject();

        Integer errorCode;
        String errorMessage;
        try {
            response = httpRequests.personCreate(new PostParameters().setPersonName(personName));
        } catch (FaceppParseException e) {
//            e.printStackTrace();
////            e.getMessage();
//            errorCode = e.getErrorCode();
//            errorMessage = e.getErrorMessage();
//
//            System.out.println(e.getHttpResponseCode());

            // Error: NAME_EXIST
//            if (errorCode == 1503) {
//                try {
//                    response = httpRequests.personGetInfo(new PostParameters().setPersonName(personName));
//                } catch (FaceppParseException e1) {
//                    e1.printStackTrace();
//                }
//            }
            try {
                response = httpRequests.personGetInfo(new PostParameters().setPersonName(personName));
            } catch (FaceppParseException e1) {
                e1.printStackTrace();
            }
        }

        System.out.println(response);

        String personId = null;
        try {
            personId = response.getString("person_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        addPerson(personId);

        return personId;
    }

    /**
     * Removes a person using its name.
     * @param personId the person's ID
     * @return the response string
     */
    String removePerson(String personId) {
        System.out.println("\n[FPP] Removing person");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.personDelete(new PostParameters().setPersonId(personId));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response.toString();
    }

    /**
     * Gets the face ID from the given result.
     * @param result a JSONObject result returned from a detect face API call
     * @return the face ID string
     */
    String getFaceId(JSONObject result) {
        String faceId = "";

        try {
            faceId = result.getJSONArray("face").getJSONObject(0).getString("face_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return faceId;
    }

    /**
     * Adds a face to a person using the person's ID and the face ID.
     * @param personId the person's ID
     * @param url the image URL
     * @return the response string
     */
    String addFace(String personId, String url) {
        JSONObject response = new JSONObject();

        String faceId = detectFace(url);

        System.out.println("\n[FPP] Adding face to person");

        try {
            response = httpRequests.personAddFace(
                    new PostParameters().setPersonId(personId).setFaceId(faceId));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response.toString();
    }

    /**
     * Removes a face from a person using the person's ID and the face ID.
     * @param personId the person's ID
     * @param faceId the face ID string
     * @return the response string
     */
    String removeFace(String personId, String faceId) {
        System.out.println("\n[FPP] Removing face");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.personRemoveFace(
                    new PostParameters().setPersonId(personId).setFaceId(faceId));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response.toString();
    }

    /**
     * Creates a person group.
     * @return the response string
     */
    String createGroup() {
        System.out.println("\n[FPP] Adding group");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.groupCreate(new PostParameters().setGroupName(groupName));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response.toString();
    }

    /**
     * Removes an existing person group.
     * @return the response string
     */
    String removeGroup() {
        System.out.println("\n[FPP] Removing group");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.groupDelete(new PostParameters().setGroupName(groupName));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response.toString();
    }

    /**
     * Gets an existing person group's info.
     * @return the response string
     */
    String getGroupInfo() {
        System.out.println("\n[FPP] Getting group info");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.groupGetInfo(new PostParameters().setGroupName(groupName));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response.toString();
    }

    /**
     * Adds a person to a group with the person's ID.
     * @param personId the person's ID
     * @return the response string
     */
    String addPerson(String personId) {
        System.out.println("\n[FPP] Adding person to group");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.groupAddPerson(
                    new PostParameters().setGroupName(groupName).setPersonId(personId));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response.toString();
    }

    /**
     * Trains a person group in preparation for identification.
     * @return the response string
     */
    String trainGroup() {
        System.out.println("\n[FPP] Training group");
        JSONObject trainResponse;
        JSONObject response = new JSONObject();

        try {
            trainResponse = httpRequests.trainIdentify(new PostParameters().setGroupName(groupName));
            response = httpRequests.getSessionSync(trainResponse.getString("session_id"));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response.toString();
    }

    /**
     * Attempts to identify the person in the image at the given URL.
     * @param url the image URL
     * @return the response JSONObject
     */
    JSONObject identifyPerson(String url) {
        System.out.println("\n[FPP] Identifying person from a url");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.recognitionIdentify(new PostParameters().setGroupName(groupName).setUrl(url));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response;
    }

    /**
     * Attempts to identify the person in the given image file.
     * @param file the image file
     * @return the response JSONObject
     */
    JSONObject identifyPerson(File file) {
        System.out.println("\n[FPP] Identifying person from a file");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.recognitionIdentify(new PostParameters().setGroupName(groupName).setImg(file));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response;
    }

    /**
     * Attempts to identify the person in the given image byte array.
     * @param data the image byte array
     * @return the response JSONObject
     */
    JSONObject identifyPerson(byte[] data) {
        System.out.println("\n[FPP] Identifying person from a byte[]");
        JSONObject response = new JSONObject();

        Integer errorCode;
        String errorMessage;
        try {
            response = httpRequests.recognitionIdentify(new PostParameters().setGroupName(groupName).setImg(data));
        } catch (FaceppParseException e) {
            errorCode = e.getErrorCode();
            errorMessage = e.getErrorMessage();
            e.printStackTrace();
        }

        System.out.println(response);
        return response;
    }

    Boolean trainPerson(String personId) {
        System.out.println("\n[FPP] Training person");
        JSONObject trainResponse;
        JSONObject response = new JSONObject();

        try {
            trainResponse = httpRequests.trainVerify(new PostParameters().setPersonId(personId));
            response = httpRequests.getSessionSync(trainResponse.getString("session_id"));
        } catch (FaceppParseException | JSONException e) {
            e.printStackTrace();
        }

        Boolean result = false;
        try {
            result = response.getJSONObject("result").getBoolean("success");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return result;
    }

    JSONObject verifyPerson(String personId, String image) {
        JSONObject response = new JSONObject();

        String faceId = detectFace(image);

        System.out.println("\n[FPP] Verifying person from a byte[]");

        try {
            response = httpRequests.recognitionVerify(
                    new PostParameters().setPersonId(personId).setFaceId(faceId).setUrl(image));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        System.out.println(response);
        return response;
    }

    public static void main(String[] args) {
        String apiKey = "c3de3abff9815f7bdf3b35347a519ccd";
        String apiSecret = "bLdQbvPADlkjoUgXsoqzu4hcFIaEnN9o";
        FacePPAPICommunicator fpp = new FacePPAPICommunicator(apiKey, apiSecret);

//        fpp.removeGroup();
//        fpp.createGroup();
//        fpp.getGroupInfo();

        String personId = fpp.createPerson("person_0");

        fpp.addFace(personId,
                "https://www.whitehouse.gov/sites/whitehouse.gov/files/images/first-family/44_barack_obama%5B1%5D.jpg");

        fpp.addFace(personId,
                "http://www.worldnewspolitics.com/wp-content/uploads/2016/02/140718-barack-obama-2115_86aea53294a878936633ec10495866b6.jpg");

        fpp.addFace(personId,
                "http://i2.cdn.turner.com/cnnnext/dam/assets/150213095929-27-obama-0213-super-169.jpg");

        fpp.addFace(personId,
                "http://a.abcnews.go.com/images/US/AP_obama8_ml_150618_16x9_992.jpg");

        fpp.addFace(personId,
                "http://www.dailystormer.com/wp-content/uploads/2015/06/2014-10-12-obama-618x402.jpg");

        fpp.addFace(personId,
                "http://media.vocativ.com/photos/2015/10/RTS2O4I-22195838534.jpg");

        fpp.addFace(personId,
                "http://i.huffpost.com/gen/2518262/images/n-OBAMA-628x314.jpg");

        fpp.addFace(personId,
                "http://cbsnews2.cbsistatic.com/hub/i/r/2015/10/09/f27caaea-86d1-41e2-bec2-97e89e5dba03/thumbnail/770x430/0a4b24d154ee526bb6811f1888e16600/presidentobamamain.jpg");

        fpp.trainPerson(personId);

        fpp.verifyPerson(personId, 
                "https://upload.wikimedia.org/wikipedia/commons/e/e9/Official_portrait_of_Barack_Obama.jpg");

        fpp.trainGroup();

        fpp.identifyPerson("https://upload.wikimedia.org/wikipedia/commons/e/e9/Official_portrait_of_Barack_Obama.jpg");

        fpp.removePerson(personId);
    }
}

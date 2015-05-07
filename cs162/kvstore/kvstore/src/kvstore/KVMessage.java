package kvstore;

import static kvstore.KVConstants.*;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;

/**
 * This is the object that is used to generate the XML based messages
 * for communication between clients and servers.
 */
public class KVMessage implements Serializable {

    private String msgType;
    private String key;
    private String value;
    private String message;

    public static final long serialVersionUID = 6473128480951955693L;

    /**
     * Copy another KVMessage
     *
     * @param kvm the KVMessage to copy
     */
    public KVMessage(KVMessage kvm) {
        this.msgType = kvm.msgType;
        this.key = kvm.key;
        this.value = kvm.value;
        this.message = kvm.message;
    }

    /**
     * Construct KVMessage with only a type.
     *
     * @param msgType the type of this KVMessage
     */
    public KVMessage(String msgType) {
        this(msgType, null);
    }

    /**
     * Construct KVMessage with type and message.
     *
     * @param msgType the type of this KVMessage
     * @param message the content of this KVMessage
     */
    public KVMessage(String msgType, String message) {
        this.msgType = msgType;
        this.message = message;
    }

    /**
     * Construct KVMessage from the InputStream of a socket.
     * Parse XML from the InputStream with unlimited timeout.
     *
     * @param  sock Socket to receive serialized KVMessage through
     * @throws KVException if we fail to create a valid KVMessage. Please see
     *         KVConstants.java for possible KVException messages.
     */
    public KVMessage(Socket sock) throws KVException {
        this(sock, 0);
    }

    /**
     * Construct KVMessage from the InputStream of a socket.
     * This constructor parse XML from the InputStream within a certain timeout
     * or with no timeout if the provided argument is 0.
     *
     * @param  sock Socket to receive serialized KVMessage through
     * @param  timeout total allowable receipt time, in milliseconds
     * @throws KVException if we fail to create a valid KVMessage. Please see
     *         KVConstants.java for possible KVException messages.
     */
    public KVMessage(Socket sock, int timeout) throws KVException {
        BufferedReader input;
        nu.xom.Builder docBuilder;
        nu.xom.Document doc;
        try {
            sock.setSoTimeout(timeout);
            input = new BufferedReader(
                    new InputStreamReader(
                    new NoCloseInputStream(sock.getInputStream())));
        } catch (Exception e) {
            //System.out.println("initial socket exception");
            throw new KVException(ERROR_COULD_NOT_RECEIVE_DATA);
        }
        //System.out.println(input);
        try {
            docBuilder = new nu.xom.Builder();
            doc = docBuilder.build(input);
            String rootName = doc.getRootElement().getLocalName();
            if (!rootName.equals("KVMessage")) {
                throw new KVException(ERROR_INVALID_FORMAT);
            }
            //System.out.println(doc.toXML());
            this.msgType = doc.getRootElement().getAttribute("type").getValue();
            //System.out.println(msgType);
            this.key = null;
            if (doc.getRootElement().getFirstChildElement("Key") != null) {
                this.key = doc.getRootElement().getFirstChildElement("Key").getValue();
                //System.out.println("parsed key correctly");
            }
            this.value = null;
            if (doc.getRootElement().getFirstChildElement("Value") != null) {
                this.value = doc.getRootElement().getFirstChildElement("Value").getValue();
            }
            this.message = null;
            if (doc.getRootElement().getFirstChildElement("Message") != null) {
                this.message = doc.getRootElement().getFirstChildElement("Message").getValue();
            }

            if (msgType.equals(PUT_REQ)) {
                if ((this.key == null) || (this.value == null)) {
                    throw new KVException(ERROR_INVALID_FORMAT);
                }
            } else if (msgType.equals(GET_REQ) || msgType.equals(DEL_REQ)) {
                if (this.key == null) {
                    //System.out.println("get request invalid format");
                    throw new KVException(ERROR_INVALID_FORMAT);
                }
            } else if (msgType.equals(RESP)) {
                if (this.message == null && (this.key == null || this.value == null)) {
                    throw new KVException(ERROR_INVALID_FORMAT);
                }
            } else if (msgType.equals(REGISTER)) {
                if (this.message == null) {
                    throw new KVException(ERROR_INVALID_FORMAT);
                }
            } else {
        	switch (msgType) {
        	    case READY: break;
        	    case COMMIT: break;
        	    case ABORT: break;
        	    case ACK: break;
        	    default: throw new KVException(ERROR_INVALID_FORMAT);
        	}
            }
        } catch (SocketException e) {
            //System.out.println("late socket exception");
            throw new KVException(ERROR_COULD_NOT_RECEIVE_DATA);
        } catch (nu.xom.ParsingException e) {
            //System.out.println("parser exception");
            throw new KVException(ERROR_PARSER);
        } catch (IOException e) {
            //System.out.println("io exception");
            throw new KVException(ERROR_COULD_NOT_RECEIVE_DATA);
        }
    }

    /**
     * Generate the serialized XML representation for this message. See
     * the spec for details on the expected output format.
     *
     * @return the XML string representation of this KVMessage
     * @throws KVException with ERROR_INVALID_FORMAT or ERROR_PARSER
     */
    public String toXML() throws KVException {
        if (this.msgType == null) {
            throw new KVException(ERROR_INVALID_FORMAT);
        }

        nu.xom.Element rootNode = new nu.xom.Element("KVMessage");
        nu.xom.Attribute msgTypeAttr = new nu.xom.Attribute("type", this.msgType);
        rootNode.addAttribute(msgTypeAttr);

        if (this.msgType.equals(PUT_REQ)) {
            if (this.key == null || this.value == null) {
                throw new KVException(ERROR_INVALID_FORMAT);
            }
            nu.xom.Element keyNode = new nu.xom.Element("Key");
            nu.xom.Element valueNode = new nu.xom.Element("Value");
            keyNode.appendChild(this.key);
            valueNode.appendChild(this.value);
            rootNode.appendChild(keyNode);
            rootNode.appendChild(valueNode);
        } else if (this.msgType.equals(GET_REQ) || this.msgType.equals(DEL_REQ)) {
            if (this.key == null) {
                throw new KVException(ERROR_INVALID_FORMAT);
            }
            nu.xom.Element keyNode = new nu.xom.Element("Key");
            keyNode.appendChild(this.key);
            rootNode.appendChild(keyNode);
        } else if (this.msgType.equals(RESP)) {
            if (this.message == null && (this.key == null || this.value == null)) {
                throw new KVException(ERROR_INVALID_FORMAT);
            }
            if (this.message != null) {
                nu.xom.Element msgNode = new nu.xom.Element("Message");
                msgNode.appendChild(this.message);
                rootNode.appendChild(msgNode);
            } else {
                nu.xom.Element keyNode = new nu.xom.Element("Key");
                nu.xom.Element valueNode = new nu.xom.Element("Value");
                keyNode.appendChild(this.key);
                valueNode.appendChild(this.value);
                rootNode.appendChild(keyNode);
                rootNode.appendChild(valueNode);
            }
        } else if (this.msgType.equals(REGISTER)) {
            if (this.message == null) {
        	throw new KVException(ERROR_INVALID_FORMAT);
            }
            nu.xom.Element messageNode = new nu.xom.Element("Message");
            messageNode.appendChild(this.message);
            rootNode.appendChild(messageNode);
        } else if (this.msgType.equals(READY)) {
        	//Do nothing.
        } else if (this.msgType.equals(COMMIT)) {
            //Do nothing.
        } else if (this.msgType.equals(ABORT)) {
            if (this.message != null) {
        	nu.xom.Element messageNode = new nu.xom.Element("Message");
        	messageNode.appendChild(this.message);
        	rootNode.appendChild(messageNode);
            } else {
        	//Do nothing.
            }
        } else if (this.msgType.equals(ACK)) {
        	//Do nothing.
        } else {
            throw new KVException(ERROR_INVALID_FORMAT);
        }
        nu.xom.Document output = new nu.xom.Document(rootNode);
        return output.toXML();
    }


    /**
     * Send serialized version of this KVMessage over the network.
     * You must call sock.shutdownOutput() in order to flush the OutputStream
     * and send an EOF (so that the receiving end knows you are done sending).
     * Do not invoke close on the socket. Closing a socket closes the InputStream
     * as well as the OutputStream, stopping the receipt of a response.
     *
     * @param  sock Socket to send XML through
     * @throws KVException with ERROR_INVALID_FORMAT, ERROR_PARSER, or
     *         ERROR_COULD_NOT_SEND_DATA
     */
    public void sendMessage(Socket sock) throws KVException {
        PrintWriter output;
        try {
            output = new PrintWriter(sock.getOutputStream(), true);
            output.println(this.toXML());
            sock.shutdownOutput();
        } catch (SocketException e) {
            throw new KVException(ERROR_COULD_NOT_SEND_DATA);
        } catch (IOException e) {
            throw new KVException(ERROR_COULD_NOT_SEND_DATA);
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsgType() {
        return msgType;
    }


    @Override
    public String toString() {
        try {
            return this.toXML();
        } catch (KVException e) {
            // swallow KVException
            return e.toString();
        }
    }

    /*
     * InputStream wrapper that allows us to reuse the corresponding
     * OutputStream of the socket to send a response.
     * Please read about the problem and solution here:
     * http://weblogs.java.net/blog/kohsuke/archive/2005/07/socket_xml_pitf.html
     */
    private class NoCloseInputStream extends FilterInputStream {
        public NoCloseInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() {} // ignore close
    }


}

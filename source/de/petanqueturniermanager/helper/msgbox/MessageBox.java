/**
* Erstellung : 26.05.2018 / Michael Massee
**/

package de.petanqueturniermanager.helper.msgbox;

import static com.google.common.base.Preconditions.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.star.awt.MessageBoxButtons;
import com.sun.star.awt.MessageBoxType;
import com.sun.star.awt.XMessageBox;
import com.sun.star.uno.XComponentContext;

public class MessageBox extends AbstractMessageBox {
	private static final Logger logger = LogManager.getLogger(MessageBox.class);

	private final MessageBoxTypeEnum type;
	private boolean forceOk = false;
	private String caption = "";
	private String message = "";

	private MessageBox(XComponentContext xContext, MessageBoxTypeEnum type) {
		super(xContext);
		checkNotNull(xContext);
		checkNotNull(type);
		this.type = type;
	}

	public static final MessageBox from(XComponentContext xContext, MessageBoxTypeEnum type) {
		return new MessageBox(xContext, type);
	}

	public final MessageBox caption(String caption) {
		this.caption = caption;
		return this;
	}

	public final MessageBox message(String message) {
		this.message = message;
		return this;
	}

	/**
	 * wenn true dann kein Box sondern ja nach msgbox typ, MessageBoxResult ok return
	 *
	 * @param result
	 * @return
	 */
	public final MessageBox forceOk(boolean forceOk) {
		this.forceOk = forceOk;
		return this;
	}

	/**
	 * @return MessageBoxResults
	 */
	public final MessageBoxResult show() {

		if (this.forceOk) {
			switch (this.type) {
			case QUESTION_YES_NO:
			case WARN_YES_NO:
				return MessageBoxResult.YES;
			case QUESTION_OK_CANCEL:
			case WARN_OK_CANCEL:
			case WARN_OK:
				return MessageBoxResult.OK;
			}
		}
		MessageBoxResult result = null;
		XMessageBox xMessageBox = null;

		switch (this.type) {
		case QUESTION_YES_NO:
			xMessageBox = this.newXMessageBox(MessageBoxType.QUERYBOX, MessageBoxButtons.BUTTONS_YES_NO);
			break;
		case QUESTION_OK_CANCEL:
			xMessageBox = this.newXMessageBox(MessageBoxType.QUERYBOX, MessageBoxButtons.BUTTONS_OK_CANCEL);
			break;
		case WARN_OK:
			xMessageBox = this.newXMessageBox(MessageBoxType.WARNINGBOX, MessageBoxButtons.BUTTONS_OK);
			break;
		case WARN_OK_CANCEL:
			xMessageBox = this.newXMessageBox(MessageBoxType.WARNINGBOX, MessageBoxButtons.BUTTONS_OK_CANCEL);
			break;
		case WARN_YES_NO:
			xMessageBox = this.newXMessageBox(MessageBoxType.WARNINGBOX, MessageBoxButtons.BUTTONS_YES_NO);
			break;
		default:
			break;
		}

		if (xMessageBox != null) {
			short msgBoxresult = xMessageBox.execute();
			result = MessageBoxResult.findResult(msgBoxresult);
		}

		return result;
	}

	private XMessageBox newXMessageBox(MessageBoxType type, int buttons) {
		return this.getXMessageBoxFactory().createMessageBox(getWindowPeer(), type, buttons, this.caption, this.message);
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
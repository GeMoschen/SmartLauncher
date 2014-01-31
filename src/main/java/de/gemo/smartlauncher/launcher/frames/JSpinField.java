package de.gemo.smartlauncher.launcher.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * JSpinField is a numeric field with 2 spin buttons to increase or decrease the
 * value. It has the same interface as the "old" JSpinField but uses a JSpinner
 * internally (since J2SE SDK 1.4) rather than a scrollbar for emulating the
 * spin buttons.
 * 
 * @author Kai Toedter
 * @version $LastChangedRevision: 85 $
 * @version $LastChangedDate: 2006-04-28 13:50:52 +0200 (Fr, 28 Apr 2006) $
 */
public class JSpinField extends JPanel implements ChangeListener, CaretListener, ActionListener, FocusListener {
    private static final long serialVersionUID = 1694904792717740650L;

    protected JSpinner spinner;

    /** the text (number) field */
    protected JTextField textField;
    protected int min;
    protected int max;
    protected int value;
    protected Color darkGreen;

    /**
     * Default JSpinField constructor. The valid value range is between
     * Integer.MIN_VALUE and Integer.MAX_VALUE. The initial value is 0.
     */
    public JSpinField() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE, 16);
    }

    /**
     * JSpinField constructor with given minimum and maximum vaues and initial
     * value 0.
     */
    public JSpinField(int min, int max) {
        this(min, max, 128);
    }

    /**
     * JSpinField constructor with given minimum and maximum vaues and initial
     * value 0.
     */
    public JSpinField(int min, int max, int step) {
        super();
        setName("JSpinField");
        this.min = min;
        if (max < min)
            max = min;
        this.max = max;
        value = 0;
        if (value < min)
            value = min;
        if (value > max)
            value = max;

        darkGreen = new Color(0, 150, 0);
        setLayout(new BorderLayout());
        textField = new JTextField();
        textField.addCaretListener(this);
        textField.addActionListener(this);
        textField.setHorizontalAlignment(SwingConstants.RIGHT);
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setText(Integer.toString(value) + " MB");
        textField.addFocusListener(this);
        spinner = new JSpinner() {
            private static final long serialVersionUID = -6287709243342021172L;
            private JTextField textField = new JTextField();

            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                return new Dimension(size.width, textField.getPreferredSize().height);
            }
        };
        spinner.setEditor(textField);
        spinner.addChangeListener(this);

        SpinnerModel model = new SpinnerNumberModel(min, min, max, step);
        spinner.setModel(model);

        add(spinner, BorderLayout.CENTER);

    }

    public void adjustWidthToMaximumValue() {
        JTextField testTextField = new JTextField(Integer.toString(max));
        int width = testTextField.getPreferredSize().width;
        int height = testTextField.getPreferredSize().height;
        textField.setPreferredSize(new Dimension(width, height));
        textField.revalidate();
    }

    /**
     * Is invoked when the spinner model changes
     * 
     * @param e
     *            the ChangeEvent
     */
    public void stateChanged(ChangeEvent e) {
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        int value = model.getNumber().intValue();
        setValue(value);
    }

    /**
     * Sets the value attribute of the JSpinField object.
     * 
     * @param newValue
     *            The new value
     * @param updateTextField
     *            true if text field should be updated
     */
    protected void setValue(int newValue, boolean updateTextField, boolean firePropertyChange) {
        int oldValue = value;
        if (newValue < min) {
            value = min;
        } else if (newValue > max) {
            value = max;
        } else {
            value = newValue;
        }

        if (updateTextField) {
            textField.setText(Integer.toString(value) + " MB");
            textField.setForeground(Color.black);
        }

        if (firePropertyChange) {
            firePropertyChange("value", oldValue, value);
        }
    }

    /**
     * Sets the value. This is a bound property.
     * 
     * @param newValue
     *            the new value
     * 
     * @see #getValue
     */
    public void setValue(int newValue) {
        setValue(newValue, true, true);
        spinner.setValue(new Integer(value));
    }

    /**
     * Returns the value.
     * 
     * @return the value value
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the minimum value.
     * 
     * @param newMinimum
     *            the new minimum value
     * 
     * @see #getMinimum
     */
    public void setMinimum(int newMinimum) {
        min = newMinimum;
    }

    /**
     * Returns the minimum value.
     * 
     * @return the minimum value
     */
    public int getMinimum() {
        return min;
    }

    /**
     * Sets the maximum value and adjusts the preferred width.
     * 
     * @param newMaximum
     *            the new maximum value
     * 
     * @see #getMaximum
     */
    public void setMaximum(int newMaximum) {
        max = newMaximum;
    }

    /**
     * Sets the horizontal alignment of the displayed value.
     * 
     * @param alignment
     *            the horizontal alignment
     */
    public void setHorizontalAlignment(int alignment) {
        textField.setHorizontalAlignment(alignment);
    }

    /**
     * Returns the maximum value.
     * 
     * @return the maximum value
     */
    public int getMaximum() {
        return max;
    }

    /**
     * Sets the font property.
     * 
     * @param font
     *            the new font
     */
    public void setFont(Font font) {
        if (textField != null) {
            textField.setFont(font);
        }
    }

    /**
     * Sets the foreground
     * 
     * @param fg
     *            the foreground
     */
    public void setForeground(Color fg) {
        if (textField != null) {
            textField.setForeground(fg);
        }
    }

    /**
     * After any user input, the value of the textfield is proofed. Depending on
     * being an integer, the value is colored green or red.
     * 
     * @param e
     *            the caret event
     */
    public void caretUpdate(CaretEvent e) {
        try {
            int testValue = Integer.valueOf(textField.getText().replace(" MB", "")).intValue();

            if ((testValue >= min) && (testValue <= max)) {
                textField.setForeground(darkGreen);
                setValue(testValue, false, true);
            } else {
                textField.setForeground(Color.red);
            }
        } catch (Exception ex) {
            if (ex instanceof NumberFormatException) {
                textField.setForeground(Color.red);
            }
        }
        textField.repaint();
    }

    /**
     * After any user input, the value of the textfield is proofed. Depending on
     * being an integer, the value is colored green or red. If the textfield is
     * green, the enter key is accepted and the new value is set.
     * 
     * @param e
     *            Description of the Parameter
     */
    public void actionPerformed(ActionEvent e) {
        if (textField.getForeground().equals(darkGreen)) {
            setValue(Integer.valueOf(textField.getText().replace(" MB", "")).intValue());
        }
    }

    /**
     * Enable or disable the JSpinField.
     * 
     * @param enabled
     *            The new enabled value
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        spinner.setEnabled(enabled);
        textField.setEnabled(enabled);
        /*
         * Fixes the background bug 4991597 and sets the background explicitely
         * to a TextField.inactiveBackground.
         */
        if (!enabled) {
            textField.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        }
    }

    /**
     * Returns the year chooser's spinner (which allow the focus to be set to
     * it).
     * 
     * @return Component the spinner or null, if the month chooser has no
     *         spinner
     */
    public Component getSpinner() {
        return spinner;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    public void focusGained(FocusEvent e) {
    }

    /**
     * The value of the text field is checked against a valid (green) value. If
     * valid, the value is set and a property change is fired.
     */
    public void focusLost(FocusEvent e) {
        actionPerformed(null);
    }
}

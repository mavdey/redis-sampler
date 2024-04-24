package ru.beeline.lt.gui;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.beeline.lt.JMeterPluginUtils;
import ru.beeline.lt.RedisSampler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;

public class RedisSamplerGui extends AbstractSamplerGui {

    private static final Logger log = LoggerFactory.getLogger(RedisSamplerGui.class);

    private static final long serialVersionUID = 1L;

    private JTextField clientNameField;
    private JTextField hostField;
    private JTextField portField;
    private JTextField passwordField;
    private JTextField databaseField;
    private JTextField timeoutFiled;

    private JComboBox<String> operationSelector;
    private JTextField keyField;
    private JTextField valueField;
    private JTextField expireField;

    public RedisSamplerGui() {
        super();
        log.debug("RedisSamplerGui()");
        createGui();
    }

    private void createGui() {
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
        setBorder(makeBorder());
        add(makeTitlePanel());

        JPanel connectionPanel = new JPanel();
        connectionPanel.setLayout(new BoxLayout(connectionPanel, BoxLayout.X_AXIS));
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Redis Connection"));
        connectionPanel.add(Box.createHorizontalStrut(10));
        JLabel hostLabel = new JLabel("Host: ");
        connectionPanel.add(hostLabel);
        hostField = new JTextField();
        hostField.setColumns(25);
        hostField.setMaximumSize(new Dimension(Integer.MAX_VALUE, hostField.getMinimumSize().height));
        hostField.setMinimumSize(new Dimension(20, hostField.getMinimumSize().height));
        connectionPanel.add(hostField);
        JLabel portLabel = new JLabel("Port: ");
        connectionPanel.add(portLabel);
        portField = new JTextField();
        addIntegerRangeCheck(portField, 1, 65535);
        portField.setColumns(5);
        portField.setMaximumSize(portField.getPreferredSize());
        connectionPanel.add(portField);

        JPanel redisParamsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel clientNameLabel = new JLabel("Client name: ");
        redisParamsPanel.add(clientNameLabel);
        clientNameField = new JTextField();
        clientNameField.setColumns(30);
        redisParamsPanel.add(clientNameField);
        redisParamsPanel.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 0));
        JLabel databaseLabel = new JLabel("Database: ");
        redisParamsPanel.add(databaseLabel);
        databaseField = new JTextField();
        databaseField.setColumns(10);
        redisParamsPanel.add(databaseField);
        JLabel databaseErrorLabel = new JLabel();
        databaseErrorLabel.setForeground(GuiUtils.getLookAndFeelColor("TextField.errorForeground"));
        addIntegerRangeCheck(databaseField, 0, Integer.MAX_VALUE, databaseErrorLabel);
        redisParamsPanel.add(databaseErrorLabel);

        JLabel timeoutLabel = new JLabel("Timeout: ");
        redisParamsPanel.add(timeoutLabel);
        timeoutFiled = new JTextField();
        timeoutFiled.setColumns(10);
        redisParamsPanel.add(timeoutFiled);
        JLabel timeoutErrorLabel = new JLabel();
        timeoutErrorLabel.setForeground(GuiUtils.getLookAndFeelColor("TextField.errorForeground"));
        addIntegerRangeCheck(timeoutFiled, 0, Integer.MAX_VALUE, timeoutErrorLabel);
        redisParamsPanel.add(timeoutErrorLabel);

        JLabel passwordLabel = new JLabel("Password: ");
        redisParamsPanel.add(passwordLabel);
        passwordField = new JTextField();
        passwordField.setColumns(10);
        redisParamsPanel.add(passwordField);

        JPanel requestPanel = new JPanel();
        requestPanel.setLayout(new BoxLayout(requestPanel, BoxLayout.X_AXIS));
        requestPanel.setBorder(BorderFactory.createTitledBorder("Redis Request"));
        requestPanel.add(Box.createHorizontalStrut(10));
        JLabel operationLabel = new JLabel("Operation: ");
        requestPanel.add(operationLabel);
        operationSelector = new JComboBox<>(new String[]{"GET", "SETEX", "EXISTS", "PEXPIRE", "DEL"});
        operationSelector.addActionListener(e -> {
            switch ((String) operationSelector.getSelectedItem()) {
                case "GET", "EXISTS", "DEL" -> {
                    valueField.setEnabled(false);
                    expireField.setEnabled(false);
                }
                case "SETEX" -> {
                    valueField.setEnabled(true);
                    expireField.setEnabled(true);
                }
                case "PEXPIRE" -> {
                    expireField.setEnabled(true);
                    valueField.setEnabled(false);
                }
            }
        });
        requestPanel.add(operationSelector);
        JLabel keyLabel = new JLabel("Key: ");
        requestPanel.add(keyLabel);
        keyField = new JTextField();
        keyField.setColumns(10);
        requestPanel.add(keyField);
        JLabel valueLabel = new JLabel("Value: ");
        requestPanel.add(valueLabel);
        valueField = new JTextField();
        valueField.setColumns(10);
        requestPanel.add(valueField);
        JLabel expireLabel = new JLabel("Expire: ");
        requestPanel.add(expireLabel);
        expireField = new JTextField();
        addIntegerRangeCheck(expireField, 0, Long.MAX_VALUE);
        expireField.setColumns(10);
        requestPanel.add(expireField);


//        add(clientNamePanel);
        add(connectionPanel);
        add(redisParamsPanel);
        add(requestPanel);
    }

    @Override
    public String getLabelResource() {
        log.debug("getLabelResource()");
        return null;
    }

    @Override
    public String getStaticLabel() {
        log.debug("getStaticLabel()");
        return JMeterPluginUtils.prefixLabel("Redis Sampler");
    }

    @Override
    public TestElement createTestElement() {
        log.debug("createTestElement()");
        RedisSampler testElement = new RedisSampler();
        configureTestElement(testElement);
        return testElement;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof RedisSampler sampler) {
            clientNameField.setText(sampler.getClientName());
            hostField.setText(sampler.getHost());
            portField.setText(sampler.getPort());
            passwordField.setText(sampler.getPassword());
            databaseField.setText(sampler.getDatabase());
            timeoutFiled.setText(sampler.getTimeout());

            operationSelector.setSelectedItem(sampler.getOperation());
            keyField.setText(sampler.getKey());
            valueField.setText(sampler.getValue());
            expireField.setText(sampler.getExpire());
        }
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        log.debug("modifyTestElement(%s)".formatted(element.getName()));
        if (element instanceof RedisSampler sampler) {
            sampler.setClientName(clientNameField.getText());
            sampler.setHost(hostField.getText());
            sampler.setPort(portField.getText());
            sampler.setPassword(passwordField.getText());
            sampler.setDatabase(databaseField.getText());
            sampler.setTimeout(timeoutFiled.getText());
            sampler.setOperation((String) operationSelector.getSelectedItem());
            sampler.setKey(keyField.getText());
            sampler.setValue(valueField.getText());
            sampler.setExpire(expireField.getText());
        }
    }

    @Override
    public void clearGui() {
        log.debug("clearGui()");
        super.clearGui();
        clientNameField.setText("jmeter_redis_client");
        hostField.setText("localhost");
        portField.setText("6379");
        passwordField.setText("");
        databaseField.setText("0");
        timeoutFiled.setText("2000");

        operationSelector.setSelectedItem("GET");
        keyField.setText("");
        valueField.setText("");
        valueField.setEnabled(false);
        expireField.setText("");
        expireField.setEnabled(false);
    }

    protected void addIntegerRangeCheck(final JTextField input, int min, long max) {
        addIntegerRangeCheck(input, min, max, null);
    }

    protected void addIntegerRangeCheck(final JTextField input, int min, long max, JLabel errorMsgField) {
        input.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkIntegerInRange(e.getDocument(), min, max, input, errorMsgField);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkIntegerInRange(e.getDocument(), min, max, input, errorMsgField);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkIntegerInRange(e.getDocument(), min, max, input, errorMsgField);
            }
        });
    }

    private boolean checkIntegerInRange(Document doc, int min, long max, JTextField field, JLabel errorMsgField) {
        boolean ok = false;
        boolean isNumber = false;

        try {
            String literalContent = JMeterPluginUtils.stripJMeterVariables(doc.getText(0, doc.getLength()));
            if (literalContent.trim().length() > 0) {
                int value = Integer.parseInt(literalContent);
                ok = value >= min && value <= max;
                isNumber = true;
            } else {
                // Could be just a JMeter variable (e.g. ${port}), which should not be refused!
                ok = true;
            }
        } catch (NumberFormatException ignored) {
        } catch (BadLocationException e) {
            // Impossible
        }
        if (field != null)
            if (ok) {
                field.setForeground(GuiUtils.getLookAndFeelColor("TextField.foreground"));
                if (errorMsgField != null)
                    errorMsgField.setText("");
            } else {
                field.setForeground(GuiUtils.getLookAndFeelColor("TextField.errorForeground"));
                if (isNumber && errorMsgField != null)
                    errorMsgField.setText("Value must >= " + min + " and <= " + max);
            }
        return ok;
    }
}

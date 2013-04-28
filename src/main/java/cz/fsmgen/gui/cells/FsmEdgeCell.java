/*
 * VYSOKÉ UČENÍ TECHNICKÉ V BRNĚ BRNO UNIVERSITY OF TECHNOLOGY
 *
 * FAKULTA INFORMAČNÍCH TECHNOLOGIÍ
 *
 * Baklářská práce
 *
 * Generátor konečných automatů z grafického popisu pro jazyk VHDL
 *
 * Author: Martin Janyš
 *
 * Brno 2013
 */
package cz.fsmgen.gui.cells;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import cz.fsmgen.gui.GraphEditor;
import static cz.fsmgen.gui.cells.CellWithProperties.initValues;
import static cz.fsmgen.gui.cells.CellWithProperties.maxVisibleLen;
import static cz.fsmgen.gui.cells.FsmStateCell.getInitValues;
import cz.fsmgen.gui.utils.StringUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cz.jvhdl.VHDL;

/**
 *
 * @author Martin
 */
public class FsmEdgeCell extends CellWithProperties {

    Set<String> undefinedVars = new HashSet<>();

    public FsmEdgeCell() {
        this.setEdge(true);
        this.setGeometry(new mxGeometry(0, 0, 120, 120));
        this.setStyle("edgeStyle=elbowEdgeStyle;rounded=1;editable=false;labelBorderColor=#000000;");
    }

    public FsmEdgeCell(String style) {
        this();
        this.setStyle(style + (!style.isEmpty() ? ";" : "") + "rounded=1;editable=false;labelBorderColor=#000000;");
    }

    public static String[] getInitValues() {
        if (initValues == null) {
            initValues = new String[]{"State", ""};
        }
        return initValues;
    }

    public static String getInitAt(int i) {
        return getInitValues()[i];
    }

    @Override
    public void initProperties() {
        propertiesFields = new String[]{/*"value",*/"inputs", "outputs"};
    }

    @Override
    public String[] getPropertiesFields() {
        if (getParent() != null && getParent() instanceof BlockCell) {
            propertiesFields = new String[]{/*"value",*/"inputs", "outputs"};
        }
        else {
            initProperties();
        }
        return this.propertiesFields;
    }

    private String output() {
        StringBuilder out = new StringBuilder();

        if (getParent() instanceof BlockCell) {
            BlockCell p = (BlockCell) getParent();
            Set<BlocksPort> mealy = p.getMealyOutputsList();

            Map<String, Object> inOut = this.getInOut();

            if (!mealy.isEmpty()) {

                boolean hasOutput = false;
                for (BlocksPort port : mealy) {
                    Object o = inOut.get(port.getName());
                    if (o != null && !o.toString().isEmpty()) {
                        if (o.toString().length() > maxVisibleLen) {
                            out.append("<expr>");
                        }
                        else {
                            out.append(o);
                        }
                        hasOutput = true;
                    }
                    else {
                        out.append("X");
                    }
                    out.append(";");
                }
                int index = out.lastIndexOf(";");
                if (index > 0) {
                    out.deleteCharAt(index);
                }

                if (!hasOutput) {
                    out = new StringBuilder();
                }
            }
        }

        return out.toString();
    }

    private String input() {
        StringBuilder in = new StringBuilder();

        if (getParent() instanceof BlockCell) {
            BlockCell p = (BlockCell) getParent();

            Collection<BlocksPort> inputsList = p.getInputsList();
            Map<String, Object> inOut = this.getInOut();

            boolean hasInput = false;
            for (BlocksPort port : inputsList) {
                Object o = inOut.get(port.getName());
                if (o != null && !o.toString().isEmpty()) {
                    if (o.toString().length() > maxVisibleLen) {
                        in.append("<expr>");
                    }
                    else {
                        in.append(o);
                    }
                    hasInput = true;
                }
                else {
                    in.append("X");
                }
                // not append if item ends with or
                if (!(o != null && o.toString().trim().toLowerCase().endsWith("or"))) {
                    in.append(" & ");
                }
                else {
                    in.append(" ");
                }
            }
            String lower = in.toString().toLowerCase();
            int index = Math.max(in.lastIndexOf(" & "),
                    lower.lastIndexOf(" or "));
            if (index > 0) {
                in.delete(index, index + 4);
            }

            if (!hasInput) {
                in = new StringBuilder();
            }
        }

        return in.toString();
    }

    @Override
    public Object getValue() {
        String out = StringUtils.htmlEncode(output()).toString();
        String in = StringUtils.htmlEncode(input()).toString();
        String delim = "";
        if (!out.isEmpty() && !in.isEmpty()) {
            delim = "<hr>";
        }
        return in + delim + out;
    }

    public String conditionStr() {
        StringBuilder condStr = new StringBuilder();

        if (getParent() instanceof BlockCell) {
            BlockCell p = (BlockCell) getParent();

            List<BlocksPort> inputsList = new ArrayList<>(p.getInputsList());
            Collections.sort(inputsList);

            Map<String, Object> inOut = this.getInOut();

            boolean hasInput = false;
            for (BlocksPort port : inputsList) {
                Object o = inOut.get(port.getName());
                if (o != null && !o.toString().isEmpty()) {
                    String c = condition(port.getName(), o.toString());

                    condStr.append(c);
                    hasInput = true;

                    if (!c.toLowerCase().endsWith("or")) {
                        addUndefined(o.toString());
                        condStr.append(" and ");
                    }
                    else {
                        String[] split = o.toString().split("\\s+");
                        if (split.length > 0) {
                            addUndefined(split[0]);
                        }
                        condStr.append(" ");
                    }
                }
//                else if (port.getDefaultValue() != null && !port.getDefaultValue().isEmpty()) {
//                    String c = condition(port.getName(), port.getDefaultValue());
//
//                    condStr.append(c);
//                    hasInput = true;
//
//                    if (!c.toLowerCase().endsWith("or")) {
//                        addUndefined(port.getDefaultValue().toString());
//                        condStr.append(" and ");
//                    }
//                    else {
//                        String[] split = port.getDefaultValue().toString().split("\\s+");
//                        if (split.length > 0) {
//                            addUndefined(split[0]);
//                        }
//                        condStr.append(" ");
//                    }
//                }
            }
            String lower = condStr.toString().toLowerCase();
            int index = Math.max(lower.lastIndexOf(" and "),
                    lower.lastIndexOf(" or "));
            if (index > 0) {
                condStr.delete(index, index + 5);
            }

            if (!hasInput) {
                condStr = new StringBuilder();
            }
        }

        return condStr.toString();
    }

    private final Pattern quotes = Pattern.compile("^\".*\"$");

    private String condition(String lvalue, String rvalue) {
        rvalue = VHDL.rvalueEscape(rvalue.trim());

        if (VHDL.keyWords.contains(rvalue)) {
            rvalue += "_1";
        }

        Matcher m = quotes.matcher(rvalue);
        if (m.matches()) { // return all in " "
            return rvalue.replaceAll("^\"|\"$", "");
        }
        else if (VHDL.testOpratorsPattern.matcher(rvalue).find()) {
            return lvalue + " " + rvalue;
        }
        else if (rvalue.toLowerCase().endsWith("or")) {
            return new StringBuilder().append(lvalue).append(" = ").append(rvalue).toString();
        }
        else {
            return new StringBuilder().append(lvalue).append(" = ").append(rvalue).toString();
        }
    }

    private void addUndefined(String var) {

        String[] split = var.split("\\s+");
        for (String string : split) {
            if (string != null && !string.isEmpty()) {
                if (!VHDL.keyWords.contains(string.toLowerCase())) {
                    if (VHDL.identifierPattern.matcher(string).matches()) {
                        undefinedVars.add(string);
                    }
                }
            }
        }
    }

    public Set<String> getUndefinedVars() {
        return undefinedVars;
    }
    Pattern sourcePort = Pattern.compile(".*sourcePort=(\\d+).*");

    public BlocksPort getSourcePort() {
        Matcher matcher = sourcePort.matcher(getStyle());
        return getTerminalPort(matcher);
    }
    Pattern targetPort = Pattern.compile(".*targetPort=(\\d+).*");

    public BlocksPort getTargetPort() {
        Matcher matcher = targetPort.matcher(getStyle());
        return getTerminalPort(matcher);
    }

    private BlocksPort getTerminalPort(Matcher matcher) {
        if (matcher.matches()) {
            String cellId = matcher.group(1);
            mxCell cell = (mxCell) ((mxGraphModel) GraphEditor.app().getGraph()
                    .getModel()).getCell(cellId);
            if (cell instanceof BlocksPort) {
                return (BlocksPort) cell;
            }
        }

        return null;
    }

//    @Override
//    public void setStyle(String style) {
//        if (style == null) {
//            style = "";
//        }
//        super.setStyle(style + ";editable=false"); //To change body of generated methods, choose Tools | Templates.
//    }
    public boolean hasOutput() {
        return !output().isEmpty();
    }
}

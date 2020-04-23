package cn.colintree.aix.AutoCompleteBox;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.AdapterView.OnItemClickListener;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.TextBox;
import com.google.appinventor.components.runtime.util.YailList;


@DesignerComponent(version = AutoCompleteBox.VERSION,
    description = "by ColinTree at http://aix.colintree.cn",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "aiwebres/icon.png")

@SimpleObject(external = true)

public class AutoCompleteBox extends AndroidNonvisibleComponent implements Component {
    public static final int VERSION = 2;
    private ComponentContainer container;
    private Context context;
    private static final String LOG_TAG = "AutoCompleteBox";
    public AutoCompleteBox(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        context = (Context) container.$context();
        Log.d(LOG_TAG, "AutoCompleteBox Created" );
        map = new HashMap<Integer, AutoCompletePopUp>();
    }

    // public static String getStackTrace(final Throwable throwable) {
    //      final StringWriter sw = new StringWriter();
    //      final PrintWriter pw = new PrintWriter(sw, true);
    //      throwable.printStackTrace(pw);
    //      return sw.getBuffer().toString();
    // }
    
    private Map<Integer, AutoCompletePopUp> map;

    private AutoCompletePopUp getAcp(TextBox textBox) {
        AutoCompletePopUp acp = null;
        int key = textBox.hashCode();
        if (map.containsKey(key)) {
            acp = map.get(key);
        }
        if (acp == null) {
            acp = new AutoCompletePopUp(context, textBox);
            map.put(key, acp);
        }
        return acp;
    }

    @SimpleFunction
    public void SetCompletion(TextBox textBox, YailList list) {
        getAcp(textBox).setList(list);
    }

    @SimpleFunction
    public YailList GetCompletion(TextBox textBox) {
        return getAcp(textBox).getList();
    }

    private boolean forAppinventor = true;
    private boolean forAppinventor_lock = false;
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(userVisible = false)
    public void ForAppinventor(boolean forAppinventor) {
        if (!forAppinventor_lock) {
            this.forAppinventor = forAppinventor;
            forAppinventor_lock = true;
        }
    }

    private class AutoCompletePopUp extends ListPopupWindow implements OnItemClickListener, TextWatcher {
        private final TextBox textBox;
        private ArrayAdapter<Spannable> adapter;
        AutoCompletePopUp(Context context, TextBox textBox) {
            this(context, textBox, YailList.makeEmptyList());
        }
        AutoCompletePopUp(Context context, TextBox textBox, YailList list) {
            super(context);

            setWidth(WRAP_CONTENT);
            setHeight(WRAP_CONTENT);
            setModal(false);
            setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);
            setAnchorView(textBox.getView());
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            //setInputMethodMode(INPUT_METHOD_NOT_NEEDED);
            
            this.textBox = textBox;
            this.list = list;
            setAdapter(list); // got one in setColor already

            setOnItemClickListener(this);
            ((EditText) textBox.getView()).addTextChangedListener(this);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (adapter == null || s.length() == 0) {
                return;
            }
            adapter.getFilter().filter(s);
            show();
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            Object selected = adapter.getItemAtPosition(position);
            textBox.Text(selected == null ? "" : selected.toString());
            dismiss();
        }

        public void setAdapter(YailList list) {
            boolean newAdapter = false;
            if (adapter == null) {
                adapter = new ArrayAdapter<Spannable>(context, (forAppinventor ? android.R.layout.simple_dropdown_item_1line : android.R.layout.simple_spinner_dropdown_item));
                newAdapter = true;
            }
            adapter.clear();
            Spannable chars;
            for(int i=0; i<list.size(); i++) {
                chars = new SpannableString(list.getString(i));
                if (forAppinventor) {
                    chars.setSpan(new ForegroundColorSpan(Component.COLOR_BLACK), 0, chars.length(), 0);
                    //chars.setSpan(new AbsoluteSizeSpan(14), 0, chars.length(), 0);
                }
                adapter.add(chars);
            }
            if (newAdapter) {
                setAdapter(adapter);
            }
        }

        private YailList list;
        public void setList(YailList list) {
            this.list = list;
            setAdapter(list);
        }
        public YailList getList() {
            return (list == null) ? YailList.makeEmptyList() : list;
        }
        
    }
}
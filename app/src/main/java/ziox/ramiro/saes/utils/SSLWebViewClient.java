package ziox.ramiro.saes.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AlertDialog;

import ziox.ramiro.saes.R;
import ziox.ramiro.saes.activities.MainActivity;

import static ziox.ramiro.saes.utils.ContextUtilsKt.isNetworkAvailable;
import static ziox.ramiro.saes.utils.SharedPreferencesUtilsKt.getPreference;
import static ziox.ramiro.saes.utils.SharedPreferencesUtilsKt.removePreference;
import static ziox.ramiro.saes.utils.SharedPreferencesUtilsKt.setPreference;


/**
 * Creado por Ramiro el 7/21/2018 a las 5:52 PM para SAES.
 */
public class SSLWebViewClient extends WebViewClient {
    private final Context context;

    public SSLWebViewClient(Context context){
        this.context = context;
    }

    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
        if(context != null && !((MainActivity)context).isFinishing()){
            if(getPreference(context, "SSLChoose", false)){
                handler.proceed();
                return;
            }

            final AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(context, R.style.DialogAlert);
            String message = "Error en el certificado SSL.";
            switch (error.getPrimaryError()) {
                case SslError.SSL_UNTRUSTED:
                    message = "La entidad de certificación no es de confianza.";
                    break;
                case SslError.SSL_EXPIRED:
                    message = "El certificado ha expirado.";
                    break;
                case SslError.SSL_IDMISMATCH:
                    message = "El nombre de host del certificado no coincide.";
                    break;
                case SslError.SSL_NOTYETVALID:
                    message = "El certificado aún no es válido.";
                    break;
            }
            message += " ¿Desea continuar igualmente?";

            builder.setTitle("Error en el certificado SSL.");
            builder.setMessage(message);
            builder.setPositiveButton("continuar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setPreference(context, "SSLChoose", true);
                    handler.proceed();
                }
            });
            builder.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try{
                        setPreference(context, "SSLChoose", false);
                        removePreference(context, "new_url_escuela");
                        context.startActivity(new Intent(context, MainActivity.class));
                    }catch (Exception e){
                        Log.e("AppException", e.toString())
                    }
                    handler.cancel();
                }
            });
            try{
                final AlertDialog dialog = builder.create();
                dialog.show();
            }catch (Exception e){
                Log.e("AppException", e.toString())
            }
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if(isNetworkAvailable(context)){
            view.loadUrl("javascript:" +
                    "if(document.getElementById(\"ctl00_leftColumn_LoginUser_UserName\") != null){" +
                    "   window.JSI.onLoginStatusChanged(false, document.getElementById('c_default_ctl00_leftcolumn_loginuser_logincaptcha_CaptchaImage').src);" +
                    "}else if(document.getElementById(\"ctl00_leftColumn_LoginStatusSession\") != null){" +
                    "   window.JSI.onLoginStatusChanged(true,null);" +
                    "}else{" +
                    "   window.JSI.error('"+url+"');" +
                    "}" +
                    "if(document.getElementsByClassName(\"failureNotification\")[2].innerText.trim().length > 0){" +
                    "   window.JSI.onLoginFailed(document.getElementsByClassName(\"failureNotification\")[2].innerText.trim());" +
                    "}");
        }
    }
}

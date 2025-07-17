package ziox.ramiro.saes.data.models;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jetbrains.annotations.NotNull;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CancellableContinuation;
import ziox.ramiro.saes.R;
import ziox.ramiro.saes.data.data_providers.ScrapResultAdapter;
import ziox.ramiro.saes.ui.screens.MainActivity;

public class SAESWebViewClient extends WebViewClient {
    final Context context;
    final String jobId;
    final CancellableContinuation<ScrapResultAdapter<Object>> continuation;
    final Function2<String, Function0<Unit>, Unit> handleResume;

    public SAESWebViewClient(
            @NotNull Context context,
            @NotNull String jobId,
            @NotNull CancellableContinuation<ScrapResultAdapter<Object>> continuation,
            @NotNull Function2<String, Function0<Unit>, Unit> handleResume
    ) {
        this.context = context;
        this.jobId = jobId;
        this.continuation = continuation;
        this.handleResume = handleResume;
    }

    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
        final Context activityContext = MainActivity.Companion.getContext();

        if(activityContext instanceof Activity && !((Activity)activityContext).isFinishing()){
            final AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(activityContext, R.style.DialogAlert);
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

            Log.d("TAG", message);

            builder.setTitle("Error en el certificado SSL.");
            builder.setMessage(message);
            builder.setPositiveButton("continuar", (dialog, which) -> handler.proceed());

            final String finalMessage = message;

            builder.setNegativeButton("cancelar", (dialog, which) -> {
                handler.cancel();
                handleResume.invoke(jobId, () -> {
                    continuation.tryResumeWithException(new Exception(finalMessage));
                    return Unit.INSTANCE;
                });
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}

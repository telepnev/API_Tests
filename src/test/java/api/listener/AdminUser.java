package api.listener;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)              // указывается где будет применяться данная анатация
@Retention(RetentionPolicy.RUNTIME)         // указываем когда эта анатация будет запускаться
public @interface AdminUser {
}

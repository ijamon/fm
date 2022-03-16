package io.cnsoft.notifier.bridge;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Jamon on 07.03.2016.
 */
public interface NotifierBridge {

    void execute(Methods method, Object data);

    @AllArgsConstructor
    @Getter
    public enum Methods {

        /*
        Старт - показываем модалку с прогресс баром;
        в параметрах передается тип операции (копирование, перемещение и т.п.) и тип прогресс бара (с известным максимом или нет).
        Если максимум известен - его значение устанавливается в 100%, текущий прогресс - 0%
        Если максимум не извествен - в прогресс баре начинается анимация типа marquee
         */
        START_OPERATION ("startOperation"),

        /*
        Обновление значения текущего прогресса (передается значение в виде процента ##.##, 0-100%)
         */
        UPDATE_PROGRESS ("updateProgress"),

        /*
        Окончание текущией операции (вызов без аргументов)
        Скрываем модалку с прогресс барами
         */
        FINISH_OPERATION("finishOperation"),

        /*
        Во время выполнения произошла ошибка - посылаем сообщение
        (подсвечиваем красным прогресс бары, меняем кнопку Отмена на Закрыть)
         */
        SEND_ERROR ("sendError"),

        /*
        Установка сообщения для прогресс бара (имя копируемого файла и т.п.)
         */
        SET_MESSAGE("setMessage"),

        /*
        Для продолжения выполнения операции необходимо, чтобы пользователь
        ответил на вопрос (перезаписывать ли уже существующий файл при копировании )
         */
        ASK_QUESTION("askQuestion");

        private String methodName;
    }
}

package utils;

import dto.UserDTO;

/**
 * ThreadLocal 是 Java 提供的一个线程局部变量，
 * 它为使用该变量的每个线程都单独创建一个独立的副本，
 * 每个线程都可以独立地改变自己的副本，而不会影响其他线程所对应的副本。
 * 这个类的作用在于方便在同一个线程的不同方法之间共享用户信息，
 * 避免了通过方法参数传递用户信息的繁琐。
 */


public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}

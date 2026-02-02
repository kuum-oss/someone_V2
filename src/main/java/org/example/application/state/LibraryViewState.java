package org.example.application.state;

import org.example.core.entity.Book;
import org.example.core.entity.User;
import java.util.ArrayList;
import java.util.List;

public class LibraryViewState {
    public enum UiViewMode { LIST, GRID }

    private ViewMode mode = ViewMode.LIBRARY;
    private UiViewMode uiViewMode = UiViewMode.LIST;
    private User currentUser;
    private boolean authenticated;
    private boolean isAdmin;
    private List<Book> localBooks = new ArrayList<>();
    private List<Book> shopBooks = new ArrayList<>();
    private List<Book> physicalShopBooks = new ArrayList<>();
    private boolean loading;
    private String status = "";
    private int progress;

    public ViewMode getMode() { return mode; }
    public void setMode(ViewMode mode) { this.mode = mode; }

    public UiViewMode getUiViewMode() { return uiViewMode; }
    public void setUiViewMode(UiViewMode uiViewMode) { this.uiViewMode = uiViewMode; }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { 
        this.currentUser = currentUser;
        this.authenticated = currentUser != null;
        this.isAdmin = currentUser != null && currentUser.isAdmin();
    }

    public boolean isAuthenticated() { return authenticated; }
    public boolean isAdmin() { return isAdmin; }

    public List<Book> getBooks() {
        if (mode == ViewMode.LIBRARY) return localBooks;
        if (mode == ViewMode.SHOP) return shopBooks;
        return physicalShopBooks;
    }

    public void setBooks(List<Book> books) {
        if (mode == ViewMode.LIBRARY) {
            this.localBooks = new ArrayList<>(books);
        } else if (mode == ViewMode.SHOP) {
            this.shopBooks = new ArrayList<>(books);
        } else {
            this.physicalShopBooks = new ArrayList<>(books);
        }
    }

    public List<Book> getLocalBooks() { return localBooks; }
    public void setLocalBooks(List<Book> localBooks) { this.localBooks = localBooks; }

    public List<Book> getShopBooks() { return shopBooks; }
    public void setShopBooks(List<Book> shopBooks) { this.shopBooks = shopBooks; }

    public List<Book> getPhysicalShopBooks() { return physicalShopBooks; }
    public void setPhysicalShopBooks(List<Book> physicalShopBooks) { this.physicalShopBooks = physicalShopBooks; }

    public boolean isLoading() { return loading; }
    public void setLoading(boolean loading) { this.loading = loading; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
}

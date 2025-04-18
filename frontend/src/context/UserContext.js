import { createContext, useContext, useEffect, useState } from 'react';

const UserContext = createContext();

export const UserProvider = ({ children }) => {
    const [isLogin, setIsLogin] = useState(false);
    const [user, setUser] = useState(null);
    const [loginChecked, setLoginChecked] = useState(false);

    const API_BASE =
        process.env.NODE_ENV === "development"
            ? "http://localhost:8080"
            : "/api";  // nginx가 proxy_pass해주는 주소

    useEffect(() => {
        const token = localStorage.getItem("onion_token");
        if (!token) {
            console.log("토큰 없음 → /auth/me 요청 안 보냄");
            setIsLogin(false);
            setLoginChecked(true);
            return;
        }

        fetch(`${API_BASE}/auth/me`, {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        })
            .then(res => {
                if (!res.ok) throw new Error();
                return res.json();
            })
            .then(() => {
                setIsLogin(true);
            })
            .catch(() => {
                setIsLogin(false);
            })
            .finally(() => {
                setLoginChecked(true);
            });
    }, []);

    return (
        <UserContext.Provider value={{ isLogin, user, loginChecked }}>
            {children}
        </UserContext.Provider>
    );
};

export const useUser = () => useContext(UserContext);

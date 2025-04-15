import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/Home';
import Cart from './pages/Cart';
import Orders from "./pages/Orders";
import ProductDetail from "./pages/ProductDetail";
import Login from "./pages/Login";
import ChatRoom from "./components/ChatRoom";
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { UserProvider } from './context/UserContext';
import SignUp from "./pages/SignUp";


function App() {
    return (
        <UserProvider>
            <Router>
                <ToastContainer position="top-center" autoClose={2000} />
                <Routes>
                    <Route path="/" element={<Layout><Home /></Layout>} />
                    <Route path="/cart" element={<Layout><Cart /></Layout>} />
                    <Route path="/orders" element={<Layout><Orders /></Layout>} />
                    <Route path="/product/:id" element={<Layout><ProductDetail /></Layout>} />
                    <Route path="/login" element={<Layout><Login /></Layout>} />
                    <Route path="/signup" element={<Layout><SignUp /></Layout>} />
                    <Route path="/chat/:roomId" element={<ChatRoom roomId="1" username="홍길동" />} />
                </Routes>
            </Router>
        </UserProvider>
    );
}

export default App;
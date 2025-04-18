import React, { useEffect, useState } from 'react';
import { useNavigate } from "react-router-dom";

function Cart() {
    const [cart, setCart] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedItems, setSelectedItems] = useState([]);
    const token = localStorage.getItem('onion_token');
    const navigate = useNavigate();

    const API_BASE =
        process.env.NODE_ENV === "development"
            ? process.env.REACT_APP_API_BASE || "http://localhost:8080"
            : "/api";

    useEffect(() => {
        async function fetchCart() {
            try {
                const res = await fetch(`${API_BASE}/cart/items`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });
                const data = await res.json();
                setCart(data);
                setSelectedItems(data.map(item => item.product.id));
            } catch (error) {
                console.error('장바구니 불러오기 실패:', error);
            } finally {
                setLoading(false);
            }
        }
        fetchCart();
    }, [token]);

    const handleCheckboxChange = (productId, checked) => {
        setSelectedItems(prev =>
            checked ? [...prev, productId] : prev.filter(id => id !== productId)
        );
    };

    const handleRemoveSelected = async () => {
        try {
            await Promise.all(
                selectedItems.map(productId =>
                    fetch(`${API_BASE}/cart/remove?productId=${productId}`, {
                        method: 'POST',
                        headers: { 'Authorization': `Bearer ${token}` },
                    })
                )
            );
            const res = await fetch(`${API_BASE}/cart/items`, {
                headers: { 'Authorization': `Bearer ${token}` },
            });
            const updatedData = await res.json();
            setCart(updatedData);
            setSelectedItems([]);
        } catch (error) {
            console.error('삭제 실패:', error);
            alert('선택된 상품 삭제에 실패했습니다.');
        }
    };

    const handlePayment = async () => {
        if (selectedItems.length !== 1) {
            alert("여행은 상품 하나당 결제됩니다. 하나만 선택해주세요.");
            return;
        }

        const res = await fetch(`${API_BASE}/ordr/createOrder`, {
            method: "POST",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            body: JSON.stringify({ selectedProductIds: selectedItems })
        });

        const data = await res.json();

        window.IMP.init("imp04844503");
        window.IMP.request_pay({
            pg: 'html5_inicis',
            merchant_uid: data.orderUid,
            name: data.orderItems[0].product.productName,
            amount: totalPrice,
        }, async (rsp) => {
            if (rsp.success) {
                await fetch(`${API_BASE}/ordr/payment`, {
                    method: "POST",
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        payment_uid: rsp.imp_uid,
                        order_uid: rsp.merchant_uid
                    })
                });

                await fetch(`${API_BASE}/participant/confirm?productId=${selectedItems[0]}`, {
                    method: "POST",
                    headers: { Authorization: `Bearer ${token}` }
                });

                alert("결제 완료!");
                navigate('/');
            } else {
                alert("결제 실패: " + rsp.error_msg);
            }
        });
    };

    if (loading) {
        return <div className="container mt-5 text-center">Loading...</div>;
    }

    if (!cart.length) {
        return <div className="container mt-5 text-center">장바구니가 비어 있습니다.</div>;
    }

    const selectedCartItems = cart.filter(item => selectedItems.includes(item.product.id));
    const totalQuantity = selectedCartItems.reduce((sum, item) => sum + item.quantity, 0);
    const totalPrice = selectedCartItems.reduce((sum, item) => sum + item.product.price * item.quantity, 0);

    return (
        <div className="container mt-5">
            <h2 className="mb-4">장바구니</h2>
            {cart.map(item => (
                <div className="card mb-3" key={item.product.id}>
                    <div className="row no-gutters">
                        <div className="col-md-2">
                            <img
                                src={item.product.imageUrl}
                                className="card-img"
                                alt={item.product.productName}
                                style={{ objectFit: 'cover', height: '100%', width: '100%' }}
                            />
                        </div>
                        <div className="col-md-7">
                            <div className="card-body">
                                <h5 className="card-title">{item.product.productName}</h5>
                                <p className="card-text mb-1">가격: {item.product.price}원</p>
                                <p className="card-text mb-1">수량: {item.quantity}</p>
                                <p className="card-text mb-1">
                                    총 가격: {item.product.price * item.quantity}원
                                </p>
                            </div>
                        </div>
                        <div className="col-md-3 d-flex align-items-center justify-content-center">
                            <input
                                type="checkbox"
                                className="form-check-input"
                                checked={selectedItems.includes(item.product.id)}
                                onChange={(e) =>
                                    handleCheckboxChange(item.product.id, e.target.checked)
                                }
                            />
                        </div>
                    </div>
                </div>
            ))}
            <div className="d-flex justify-content-between align-items-center mt-4">
                <div>
                    <h5>총 수량: {totalQuantity}</h5>
                    <h5>총 가격: {totalPrice}원</h5>
                </div>
                <div>
                    <button className="btn btn-danger mr-3" onClick={handleRemoveSelected}>
                        선택 삭제
                    </button>
                    <button className="btn btn-primary" onClick={handlePayment}>
                        선택 결제
                    </button>
                </div>
            </div>
        </div>
    );
}

export default Cart;

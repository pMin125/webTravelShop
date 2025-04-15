import React, { useEffect, useState } from "react";

function OrderProductPage() {
    const [products, setProducts] = useState([]);
    const [error, setError] = useState("");

    const API_BASE =
        process.env.NODE_ENV === "development"
            ? process.env.REACT_APP_API_BASE || "http://localhost:8080"
            : "/api";

    useEffect(() => {
        const fetchOrders = async () => {
            try {
                const res = await fetch(`${API_BASE}/ordr/orderProduct`, {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("onion_token")}`,
                    },
                });

                if (!res.ok) {
                    throw new Error("주문 상품 조회 실패");
                }

                const data = await res.json();
                setProducts(data);
            } catch (err) {
                setError(err.message);
            }
        };

        fetchOrders();
    }, [API_BASE]);

    return (
        <div className="container mt-5">
            <h2 className="mb-4">내가 주문한 상품</h2>

            {error && <div className="alert alert-danger">{error}</div>}

            {products.length === 0 && !error ? (
                <p>주문한 상품이 없습니다.</p>
            ) : (
                products.map((product) => (
                    <div className="card mb-3" key={product.id}>
                        <div className="row no-gutters">
                            <div className="col-md-2">
                                <img
                                    src={product.imageUrl}
                                    className="card-img"
                                    alt={product.productName}
                                    style={{ objectFit: "cover", height: "100%", width: "100%" }}
                                />
                            </div>
                            <div className="col-md-10">
                                <div className="card-body">
                                    <h5 className="card-title">{product.productName}</h5>
                                    <p className="card-text mb-1">가격: {product.price}원</p>
                                    <p className="card-text text-muted">
                                        주문일: {new Date(product.createdDate).toLocaleString()}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                ))
            )}
        </div>
    );
}

export default OrderProductPage;

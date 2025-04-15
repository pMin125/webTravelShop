import React from 'react';
import Header from './Header';
import Footer from './Footer';

function Layout({ banner, children }) {
    return (
        <div>
            <Header />
            {banner && <div>{banner}</div>}
            <main className="container">{children}</main>
            <Footer />
        </div>
    );
}

export default Layout;
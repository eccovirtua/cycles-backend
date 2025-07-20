import React, { useState, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function LoginPage() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleLogin = async (e: FormEvent) => {
        e.preventDefault();

        const response = await fetch("http://localhost:8080/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: email, password }),
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem("token", data.jwtToken);
            login(email);
            navigate("/dashboard");
        } else {
            alert("Credenciales incorrectas");
        }
    };

    return (
        <div className="p-4 max-w-md mx-auto">
            <h1 className="text-2xl mb-4">Login</h1>
            <form onSubmit={handleLogin} className="flex flex-col gap-3">
                <input
                    type="email"
                    placeholder="Correo"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="border p-2"
                    required
                />
                <input
                    type="password"
                    placeholder="Contraseña"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="border p-2"
                    required
                />
                <button type="submit" className="bg-blue-600 text-white p-2 rounded">
                    Iniciar sesión
                </button>
            </form>
        </div>
    );
}
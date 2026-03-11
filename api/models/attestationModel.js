const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Attestation = sequelize.define('attestation', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    pdf_path: {
        type: DataTypes.STRING(500),
        allowNull: true
    },
    date_signature: {
        type: DataTypes.DATE,
        allowNull: true
    }
}, {
    timestamps: false, 
    underscored: true,
})

module.exports = Attestation